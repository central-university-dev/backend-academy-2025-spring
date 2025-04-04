package tbank.ab.mq

import cats.effect._
import cats.syntax.all._
import fs2._
import fs2.kafka._
import org.apache.kafka.clients.consumer.ConsumerRecord
import tbank.ab.service.AnimalService
import tbank.ab.config.KafkaConsumerConfig
import tbank.ab.domain.animal.AnimalInfo
import tbank.ab.domain.animal.AnimalId
import tbank.ab.wiring.Services
import tbank.ab.domain.habitat.Habitat
import tethys._
import tethys.jackson._

trait AnimalConsumer[F[_]] {
  def consume(key: AnimalId, value: AnimalInfo): F[Unit]
}

object AnimalConsumer {

  private class Impl(animalService: AnimalService[IO]) extends AnimalConsumer[IO] {
    override def consume(key: AnimalId, value: AnimalInfo): IO[Unit] =
      animalService.updateAnimalInfo(key, value).void
  }

  given keyDeserializer: Deserializer[IO, AnimalId] =
    Deserializer
      .string[IO]
      .map { str =>
        AnimalId(str)
      }

  given valueDeserializer: Deserializer[IO, Either[Throwable, AnimalInfo]] =
    Deserializer
      .string[IO]
      .map { json =>
        json.jsonAs[AnimalInfo]
      }

  def run(using config: KafkaConsumerConfig, services: Services): Resource[IO, Unit] = {
    val consumer = new Impl(services.animalService)
    KafkaConsumer
      .stream(ConsumerSettings[IO, AnimalId, Either[Throwable, AnimalInfo]].withProperties(config.properties))
      .subscribeTo(config.topic)
      .records
      .evalMap { committable =>
        committable.record.value match
          case Left(err) => IO.unit
          case Right(value) => consumer.consume(committable.record.key, value)
      }
      .compile
      .drain
      .toResource
  }
}
