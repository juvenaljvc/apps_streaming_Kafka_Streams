package serdes

import org.apache.kafka.common.serialization.Serializer
import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.util

class BytesSerializer[T] extends Serializer[T] {

  override def configure(configs: util.Map[String, _], isKey: Boolean) : Unit = {}

  override def serialize(topic: String, data: T): Array[Byte] = {
    if(data == null) {
      return null
    } else {
      try {

        // c'est ici qu'on procède à la sérialisation
        val bts = new ByteArrayOutputStream()
        val ost = new ObjectOutputStream(bts)

        ost.writeObject(data)
        ost.close()

        bts.toByteArray

      } catch {
        // gestionnaire d'erreur de la bibliothèque de SerDes que vous aurez choisi.
        case e : Exception  => throw new Exception(s"Erreur dans la sérialisation de  ${data.getClass.getName}. Détails de l'erreur : ${e}")

      }

    }

  }

  override def close(): Unit =  {}

}
