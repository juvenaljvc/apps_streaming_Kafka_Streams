import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.kstream.KTable
import org.apache.kafka.streams.scala.kstream.KGroupedStream
import org.apache.kafka.streams.scala.kstream.KGroupedTable
import org.apache.kafka.streams.scala.kstream.SessionWindowedKStream
import org.apache.kafka.streams.scala.kstream.TimeWindowedKStream
import org.apache.kafka.streams.state._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes.String

import java.util.Properties
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.Serdes.String
import org.apache.kafka.streams.StreamsConfig

object App_streaming {

  def main(args: Array[String]): Unit = {

    val props : Properties = new Properties()
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once")




  }

  def producer_kafka() : Unit = {

    val props : Properties = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("auto.offset.reset", "latest")
    props.put("enable.auto.commit", "false")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer_Kafka = new KafkaProducer[String, String](props)

    val cle : String = "1"
    val record_publish = new ProducerRecord[String, String]("streams_app", cle, "test 5")
    producer_Kafka.send(record_publish)

  }


}
