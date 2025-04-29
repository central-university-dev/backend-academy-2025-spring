package tbank.ab.mq

import cats.effect._
import cats.syntax.all._
import fs2._
import fs2.kafka._
import tbank.ab.config.KafkaProducerConfig
import tbank.ab.domain.animal.AnimalId
import tbank.ab.domain.animal.AnimalInfo
import tbank.ab.domain.habitat.Habitat
import tbank.ab.service.AnimalService
import tbank.ab.wiring.Services
import tethys._
import tethys.jackson._

trait AnimalUpdateProducer[F[_]] {
  def produce(animalId: AnimalId): F[Unit]
}

object AnimalUpdateProducer {

  private class Impl(topic: String, kafkaProducer: KafkaProducer[IO, AnimalId, AnimalId])
      extends AnimalUpdateProducer[IO] {
    override def produce(animalId: AnimalId): IO[Unit] =
      kafkaProducer
        .produceOne(ProducerRecord(topic, animalId, animalId))
        .flatten
        .void
  }

  given Serializer[IO, AnimalId] =
    Serializer.instance[IO, AnimalId]((_, _, animalId) => IO.pure(animalId.toString.getBytes("UTF-8")))

  def make(using config: KafkaProducerConfig): Resource[IO, AnimalUpdateProducer[IO]] =
    KafkaProducer
      .resource(ProducerSettings[IO, AnimalId, AnimalId].withProperties(config.properties))
      .map(producer => new Impl(config.topic, producer))
}
