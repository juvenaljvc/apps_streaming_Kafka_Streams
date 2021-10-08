package serdes
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.util
import com.fasterxml.jackson.annotation.JsonInclude
import org.apache.kafka.common.serialization.Deserializer
import schemas.Facture

import scala.reflect._

class JSONDeserializer extends Deserializer[Facture]{

  val objetMapper : ObjectMapper = new ObjectMapper()
  objetMapper.registerModule(DefaultScalaModule)
  objetMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  objetMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true)
  objetMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, data: Array[Byte]): Facture = {

    try {

      // processus de désérialisation ici
      val d = objetMapper.readValue(data, classOf[Facture])
      d

    } catch {
      case e: Exception => throw new Exception(s"Erreur dans la désérialisation du message. Détails de l'erreur : ${e}")
    }

  }

  override def close(): Unit = {}

}
