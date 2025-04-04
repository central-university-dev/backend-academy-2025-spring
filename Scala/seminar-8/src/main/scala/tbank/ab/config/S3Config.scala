package tbank.ab.config

import eu.timepit.refined.types.string.NonEmptyString
import fs2.aws.s3.models.Models.BucketName
import pureconfig.ConfigConvert.{catchReadError, viaNonEmptyStringOpt}
import pureconfig.ConfigReader

case class S3Config(
  uri: String,
  accessKey: String,
  secretKey: String,
  bucket: BucketName
) derives ConfigReader

object S3Config {

  given ConfigReader[BucketName] =
    viaNonEmptyStringOpt[BucketName](str => NonEmptyString.from(str).toOption.map(BucketName(_)), _.value.value)

}
