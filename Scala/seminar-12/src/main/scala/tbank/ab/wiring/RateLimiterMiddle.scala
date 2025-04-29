package tbank.ab.wiring

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.effect.std.Backpressure
import org.http4s.{HttpRoutes, Request, Response, Status}

object RateLimiterMiddle {
  def create[F[_]: MonadThrow](service: HttpRoutes[F], backpressure: Backpressure[F]): HttpRoutes[F] =
    Kleisli { (req: Request[F]) =>
      OptionT(
        backpressure.metered(
          service(req).value
        )
      ).map {
        case Some(res) => res
        case None      => Response[F](Status.TooManyRequests) // logging, metrics, error code
      }
    }
}
