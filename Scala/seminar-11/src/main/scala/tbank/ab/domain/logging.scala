package tbank.ab.domain

import cats.data.ReaderT
import cats.effect.IO
import cats.syntax.flatMap.*
import cats.{Applicative, FlatMap, Functor, ~>}
import tofu.WithProvide
import tofu.logging.Loggable
import tofu.logging.derivation.*
import tofu.syntax.funk.*
import cats.implicits.*
import tofu.generate.GenUUID

import java.util.UUID

type RequestIO[A] = ReaderT[IO, RequestContext, A]

case class RequestContext(traceId: UUID) extends LogContext derives Loggable

object RequestContext {

  def create[F[_]: GenUUID: Functor]: F[RequestContext] =
    GenUUID[F].randomUUID.map(RequestContext.apply)
  
}

trait SetupContext[I[_], F[_]] {
  def setup[A](fa: F[A]): I[A]

  def setupK: F ~> I = funK[F, I](setup)
}

object SetupContext {
  def make[I[_]: FlatMap, F[_], Ctx](from: I[Ctx])(using withProvide: WithProvide[F, I, Ctx]): SetupContext[I, F] =
    new SetupContext[I, F] {
      override def setup[A](fa: F[A]): I[A] = from >>= withProvide.runContext(fa)
    }
}