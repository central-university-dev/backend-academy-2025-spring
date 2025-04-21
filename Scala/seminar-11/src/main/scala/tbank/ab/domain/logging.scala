package tbank.ab.domain

import cats.data.ReaderT
import cats.effect.IO
import cats.~>
import tofu.logging.Loggable
import tofu.logging.derivation.*

type RequestIO[A] = ReaderT[IO, RequestContext, A]

case class RequestContext(traceId: String) derives Loggable

import tofu.syntax.funk._

class SetupContext[I[_], F[_]] {
  def setup[A](fa: F[A]): I[A]

  def setupK: F ~> I = funK[F, I](setup)
}
