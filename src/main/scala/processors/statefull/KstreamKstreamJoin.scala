package processors.statefull

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology, scala}
import org.apache.kafka.streams.scala.kstream._
import schemas.{Commande, CommandeComplet, DetailsCommande}
import serdes.{JSONDeserializerCmdComplet, JSONDeserializerCommandes, JSONDeserializerDtlCommandes, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.{JoinWindows, Printed}


import java.time.Duration
import java.util.Properties

// jointure KStream-KStream
object KstreamKstreamJoin extends App {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  implicit val jsonSerdesCommandes : Serde[Commande] = Serdes.serdeFrom(new JSONSerializer[Commande], new JSONDeserializerCommandes)
  implicit val jsonSerdesDetailsCommandes : Serde[DetailsCommande] = Serdes.serdeFrom(new JSONSerializer[DetailsCommande], new JSONDeserializerDtlCommandes)
  implicit val jsonSerdesCommandesComplet : Serde[CommandeComplet] = Serdes.serdeFrom(new JSONSerializer[CommandeComplet], new JSONDeserializerCmdComplet)

  implicit val consumedCommandes : Consumed[String, Commande] = Consumed.`with`(new CommandeTimeStampExtractor)(String, jsonSerdesCommandes)
  implicit val consumedDetailsCommandes: Consumed[String, DetailsCommande] = Consumed.`with`(new DtlsCmdTimeStampExtractor)(String, jsonSerdesDetailsCommandes)

  implicit val produced : Produced[String, CommandeComplet] = Produced.`with`(String, jsonSerdesCommandesComplet)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-kstream-join10")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val kstrCommande : KStream[String, Commande]  = str.stream[String, Commande]("commande")(consumedCommandes)
  val kstrDtlCommande : KStream[String, DetailsCommande]  = str.stream[String, DetailsCommande]("DetailsCommande")(consumedDetailsCommandes)

  val kstrDtlCommandeGood : KStream[String, DetailsCommande] = kstrDtlCommande.selectKey((k, v) => v.orderid)  // changement de clé

  val kjoin = kstrDtlCommandeGood.join(kstrCommande)((d : DetailsCommande, c : Commande) =>
    {
      CommandeComplet(d.orderid, d.productid, d.shipdate, d.billdate, d.unitprice, d.numunits, d.totalprice, c.city, c.state)
    },
    JoinWindows.of(Duration.ofMinutes(5))
  )

 // kjoin.to("commandeComplet")(produced)

  kjoin.print(Printed.toSysOut().withLabel("Jointure KStreams-à-KStreams"))


  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }
}
