package serdes
import org.apache.kafka.common.serialization.Deserializer
import java.util
import schemas.Facture

class GenericDeserializer[T] extends Deserializer[T]{

  private val genT  = classOf[T]

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, data: Array[Byte]): T = {

    try {
          // processus de désérialisation ici
       genT.newInstance()

    } catch {
      case e: Exception => throw new Exception(s"Erreur dans la désérialisation du message. Détails de l'erreur : ${e}")
    }

  }

  override def close(): Unit = {}

}
