package processors.statefull
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor
import schemas.DetailsCommande

import java.text.SimpleDateFormat
import java.time.Instant

class DtlsCmdTimeStampExtractor extends TimestampExtractor  {
  override def extract(record: ConsumerRecord[AnyRef, AnyRef], previousTimeStamp: Long): Long = {

    record.value() match {
      case r : DetailsCommande => {
        val billDate = Instant.parse(r.billdate).toEpochMilli
        billDate
      }
      case _  =>  throw new RuntimeException(s" erreur dans le parsing. Les mesages ne sont pas des instances des détails de commande : ${record.value()}")
    }

  }
}

