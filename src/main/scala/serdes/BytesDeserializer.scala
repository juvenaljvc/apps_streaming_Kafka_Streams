package serdes
import org.apache.kafka.common.serialization.Deserializer
import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.util

class BytesDeserializer [T] extends Deserializer[T]{

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, data: Array[Byte]): T = {

    try {
      // processus de désérialisation ici
      val btos = new ByteArrayInputStream(data)
      val oist = new ObjectInputStream(btos)

      oist.readObject().asInstanceOf[T]

    } catch {
      case e: Exception => throw new Exception(s"Erreur dans la désérialisation du message. Détails de l'erreur : ${e}")
    }

  }

  override def close(): Unit = {}

}