package tbank.ab.repository.model

import tbank.ab.domain.habitat.Habitat

case class AnimalInfoRepository(
  id: Int,
  name: String,
  description: String,
  habitat: Habitat,
  domesticatedYear: Option[Int]
)
