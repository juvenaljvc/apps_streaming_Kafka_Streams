package processors.statefull

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import schemas.{Facture, OrderLine}
import serdes.{JSONDeserializer, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{Printed, SessionWindows, TimeWindows, Windowed}

import java.time.{Duration, ZoneOffset}
import java.util.Properties


object WindowingProcess extends App {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String
  import org.apache.kafka.streams.scala.Serdes._

  implicit val jsonSerdes : Serde[Facture] = Serdes.serdeFrom(new JSONSerializer[Facture], new JSONDeserializer)
  implicit val consumed : Consumed[String, Facture] = Consumed.`with`(new FactureTimeStamExtractor)(Serdes.String(), jsonSerdes)
  implicit val produced : Produced[String, Facture] = Produced.`with`(Serdes.String(), jsonSerdes)


  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "windowingProcess-interm2")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, "org.apache.kafka.streams.processor.WallclockTimestampExtractor")
  props.put("message.timestamp.type", "LogAppendTime")

  val str : StreamsBuilder = new StreamsBuilder
  val kstrFacture : KStream[String, Facture]  = str.stream[String, Facture]("factureJson")(consumed)

  // fenêtres fixes
  val kCA : KTable[Windowed[String], Long]= kstrFacture
    .map((k, f) => (k, f.total))
    .groupBy((k, t) => k)(Grouped.`with`(String, Double))
    .windowedBy(TimeWindows.of(Duration.ofSeconds(15)).grace(Duration.ofSeconds(5)))  // rajout d'une période de grace pour clôturer la fenêtre
    .count()

  // fenêtres glissantes
  val kCA1 : KTable[Windowed[String], Long]= kstrFacture
    .map((k, f) => (k, f.total))
    .groupBy((k, t) => k)(Grouped.`with`(String, Double))
    .windowedBy(TimeWindows.of(Duration.ofSeconds(15)).advanceBy(Duration.ofSeconds(3)))
    .count()

  // Session
  val kCA3 : KTable[Windowed[String], Long]= kstrFacture
    .map((k, f) => (k, f.total))
    .groupBy((k, t) => k)(Grouped.`with`(String, Double))
    .windowedBy(SessionWindows.`with`(Duration.ofMinutes(5)))
    .count()

  kCA.toStream.foreach(
    (key, value) => println(
      s"clé de la fenêtre : ${key}, " +
      s"clé du message : ${key.key()}, " +
      s"debut : ${key.window().startTime().atOffset(ZoneOffset.UTC)}, " +
      s"fin : ${key.window().endTime().atOffset(ZoneOffset.UTC)}, " +
      s"valeur : ${value}"
    )
  )

  // calcul du chiffre d'affaire moyen réalisé toutes les 15 secondes
  val kCA4 = kstrFacture
    .map((k, f) => ("1", f))
    .groupBy((k, t) => k)
    .windowedBy(TimeWindows.of(Duration.ofSeconds(15)))
    .aggregate[Facture](Facture("", "", 0, 0D, OrderLine("", "", "", "", 0D,0D, 0)))(
      (key, newFacture, aggFacture) =>
        Facture(newFacture.factureid, "",
          newFacture.qantite + aggFacture.qantite,
          newFacture.total + aggFacture.total, OrderLine("", "", "", "", 0D,0D, 0))
    ).mapValues(f => (f.qantite, f.total, f.total/f.qantite))


  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }

}






