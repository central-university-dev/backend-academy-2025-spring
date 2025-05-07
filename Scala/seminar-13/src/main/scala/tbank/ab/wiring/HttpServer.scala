package tbank.ab.wiring

import cats.{~>, Applicative, Monad}
import cats.data.{Kleisli, ReaderT}
import cats.effect.{Async, Resource}
import cats.implicits.*
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.otel4s.trace.Tracer
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.impl.cats.implicits.asyncMonadError
import sttp.monad.MonadError
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.tracing.otel4s.Otel4sTracing
import sttp.tapir.swagger.SwaggerUI
import tbank.ab.config.{ServerConfig, ZoneConfig}
import tbank.ab.domain.RequestContext
import tofu.WithProvide
import tofu.generate.GenUUID
import tofu.logging.Logging
import tofu.syntax.logging.*

final case class HttpServer private (
  public: Server,
  monitoring: Server
)

object HttpServer {

  private def withDocs[R, F[_]](
    endpoints: List[ServerEndpoint[R, F]],
    config: ZoneConfig
  ): List[ServerEndpoint[R, F]] = {
    val openApi: String =
      OpenAPIDocsInterpreter(OpenAPIDocsOptions.default)
        .toOpenAPI(es = endpoints.map(_.endpoint), config.name, "1.0")
        .toYaml

    if (config.swaggerEnabled) endpoints ::: SwaggerUI[F](openApi) else endpoints
  }

  private def registerZone[I[_]: Async](
    endpoints: List[ServerEndpoint[Fs2Streams[I] & WebSockets, I]],
    config: ZoneConfig,
    options: Http4sServerOptions[I]
  ): Resource[I, Server] =
    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp(ws =>
        Router("/" -> Http4sServerInterpreter[I](options).toWebSocketRoutes(endpoints)(ws)).orNotFound
      )
      .build

  private def registerZoneWithLogging[I[_]: Async](
    endpoints: List[ServerEndpoint[Any, ReaderT[I, RequestContext, *]]],
    metricsEndpoint: ServerEndpoint[Any, I],
    config: ZoneConfig,
    options: Http4sServerOptions[I]
  )(using withProvide: WithProvide[ReaderT[I, RequestContext, *], I, RequestContext]): Resource[I, Server] = {

    val endpointsI: List[ServerEndpoint[Any, I]] = endpoints.map { endpoint =>
      transformKF[Any, I](endpoint, RequestContext.setupK[I, ReaderT[I, RequestContext, *]])
    }

    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp { ws =>
        Router(
          "/" -> Http4sServerInterpreter[I](options)
            .toWebSocketRoutes(endpointsI :+ metricsEndpoint)(ws)
        ).orNotFound
      }
      .build
  }

  private def logZoneUri[F[_]: Monad](zone: String, server: Server, conf: ZoneConfig)(using
    logging: Logging[F]
  ): F[Unit] =
    info"$zone zone started at ${server.baseUri.renderString}" >>
    Monad[F].whenA(conf.swaggerEnabled)(
      info"SSwagger UI for $zone zone available at ${server.baseUri.renderString}/docs"
    )

  def startServer[I[_]: Async](using
    publicApi: PublicApi[ReaderT[I, RequestContext, *]],
    monitoringApi: MonitoringApi[I],
    conf: ServerConfig,
    make: Logging.Make[I],
    tracer: Tracer[I]
  ): Resource[I, HttpServer] = {
    val publicEndpoints     = withDocs(publicApi.endpoints, conf.public)
    val monitoringEndpoints = withDocs(monitoringApi.endpoints, conf.monitoring)

    given Logging[I] = make.forService[HttpServer.type]

    val prometheusMetrics = PrometheusMetrics.default[I]()

    val options = Http4sServerOptions.default[I]
      .prependInterceptor(Otel4sTracing(tracer))
      .prependInterceptor(prometheusMetrics.metricsInterceptor())

    for {
      _          <- Resource.make(info"Starting server...")(_ => info"Server closed")
      public     <- registerZoneWithLogging[I](publicEndpoints, prometheusMetrics.metricsEndpoint, conf.public, options)
      monitoring <- registerZone[I](monitoringEndpoints, conf.monitoring, options)
      _          <- Resource.make(info"Server started")(_ => info"Closing server...")

      _ <- Resource.eval(logZoneUri[I]("public", public, conf.public))
      _ <- Resource.eval(logZoneUri[I]("monitoring", monitoring, conf.monitoring))
    } yield HttpServer(public, monitoring)
  }

  // def transformKF[S, F[_], G[_]](e: ServerEndpoint[S, F], mapK: F ~> G)(using
  //     fMonad: MonadError[F]
  // ): ServerEndpoint[S, G] =
  //  ServerEndpoint[e.SECURITY_INPUT, e.PRINCIPAL, e.INPUT, e.ERROR_OUTPUT, e.OUTPUT, S, G](
  //    e.endpoint,
  //    _ => a => mapK(e.securityLogic(fMonad)(a)),
  //    _ => u => i => mapK(e.logic(fMonad)(u)(i))
  //  )

  def transformKF[S, I[_]: Monad](
    e: ServerEndpoint[S, ReaderT[I, RequestContext, *]],
    mapK: ReaderT[I, RequestContext, *] ~> I
  )(using
    fMonad: MonadError[ReaderT[I, RequestContext, *]]
  ): ServerEndpoint[S, I] =
    ServerEndpoint[e.SECURITY_INPUT, (e.PRINCIPAL, RequestContext), e.INPUT, e.ERROR_OUTPUT, e.OUTPUT, S, I](
      e.endpoint,
      _ =>
        a =>
          mapK(
            for {
              result <- e.securityLogic(fMonad)(a)
              ctx    <- ReaderT.ask[I, RequestContext]
            } yield result.map(_ -> ctx)
          ),
      _ => { case (u, ctx) => i => e.logic(fMonad)(u)(i).run(ctx) }
    )
}
