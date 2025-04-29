package tbank.ab.domain

import cats.{~>, FlatMap}
import cats.data.ReaderT
import cats.effect.IO
import cats.implicits.*
import cats.syntax.flatMap.*
import tofu.WithProvide
import tofu.generate.GenUUID
import tofu.logging.Loggable
import tofu.logging.derivation.*
import tofu.syntax.funk.*

import java.util.UUID

type RequestIO[A] = ReaderT[IO, RequestContext, A]

case class RequestContext(traceId: UUID) derives Loggable

object RequestContext {

  def setupK[I[_]: FlatMap: GenUUID, F[_]](using withProvide: WithProvide[F, I, RequestContext]): F ~> I =
    new (F ~> I) {
      override def apply[A](fa: F[A]): I[A] =
        GenUUID[I].randomUUID.flatMap(uuid => withProvide.runContext(fa)(RequestContext(uuid)))
    }

}
