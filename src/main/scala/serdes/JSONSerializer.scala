package serdes
import com.fasterxml.jackson.annotation.JsonInclude
import org.apache.kafka.common.serialization.Serializer
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.util

class JSONSerializer[T] extends Serializer[T] {

  val objetMapper : ObjectMapper = new ObjectMapper()
  objetMapper.registerModule(DefaultScalaModule)
  objetMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  objetMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
  objetMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)

  override def configure(configs: util.Map[String, _], isKey: Boolean) : Unit = {}

  override def serialize(topic: String, data: T): Array[Byte] = {
    if(data == null) {
      return null
    } else {
      try {

        // c'est ici qu'on procède à la sérialisation
        val sb = objetMapper.writeValueAsBytes(data)
        sb

      } catch {
        // gestionnaire d'erreur de la bibliothèque de SerDes que vous aurez choisi.
        case e : Exception  => throw new Exception(s"Erreur dans la sérialisation de  ${data.getClass.getName}. Détails de l'erreur : ${e}")

      }

    }

  }

  override def close(): Unit =  {}

}
