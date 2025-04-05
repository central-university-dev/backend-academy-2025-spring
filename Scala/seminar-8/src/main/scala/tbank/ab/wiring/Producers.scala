package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.service.{AnimalService, AuthService, ChatService, HabitatService}
import tbank.ab.mq.AnimalUpdateProducer
import cats.effect.kernel.Resource

final case class Producers()(using
  val animalUpdateProducer: AnimalUpdateProducer[IO],
)

object Producers:
  def make(using config: AppConfig): Resource[IO, Producers] = {
    import config.given

    val animalUpdateProducer: Resource[IO, AnimalUpdateProducer[IO]]  = AnimalUpdateProducer.make

    animalUpdateProducer.map(Producers()(using _))
  }
