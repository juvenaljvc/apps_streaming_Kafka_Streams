package processors.statefull

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor
import schemas.Commande
import java.time.Instant

class CommandeTimeStampExtractor extends TimestampExtractor {
  override def extract(record: ConsumerRecord[AnyRef, AnyRef], previousTimeStamp: Long): Long = {

    record.value() match {
      case r : Commande => {
        val billDate = Instant.parse(r.orderdate).toEpochMilli
        billDate
      }
      case _  =>  throw new RuntimeException(s" erreur dans le parsing. Les mesages ne sont pas des instances de commande : ${record.value()}")
    }

  }
}





