package processors.statefull

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.apache.kafka.streams.scala.kstream._
import schemas.{Commande, CommandeComplet, DetailsCommande, Facture}
import serdes.{JSONDeserializerCommandes, JSONDeserializerDtlCommandes, JSONSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.JoinWindows

import java.time.Duration
import java.util.Properties

// jointure KStream-KStream
object KstreamKstreamJoin extends App {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String
  import org.apache.kafka.streams.scala.Serdes.Long

  implicit val jsonSerdesCommandes : Serde[Commande] = Serdes.serdeFrom(new JSONSerializer[Commande], new JSONDeserializerCommandes)
  implicit val jsonSerdesDetailsCommandes : Serde[DetailsCommande] = Serdes.serdeFrom(new JSONSerializer[DetailsCommande], new JSONDeserializerDtlCommandes)

  implicit val consumedCommandes : Consumed[Long, Commande] = Consumed.`with`(new CommandeTimeStampExtractor)(Long, jsonSerdesCommandes)
  implicit val consumedDetailsCommandes: Consumed[Long, DetailsCommande] = Consumed.`with`(new DtlsCmdTimeStampExtractor)(Long, jsonSerdesDetailsCommandes)

  implicit val produced : Produced[Long, CommandeComplet] = Produced.`with`(Long, String)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-kstream-join")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put("message.timestamp.type", "LogAppendTime")

  val str : StreamsBuilder = new StreamsBuilder
  val kstrCommande : KStream[Long, Commande]  = str.stream[Long, Commande]("commande")(consumedCommandes)
  val kstrDtlCommande : KStream[Long, DetailsCommande]  = str.stream[Long, DetailsCommande]("commande")(consumedDetailsCommandes)

  val kjoin = kstrDtlCommande.join(kstrCommande)((d : DetailsCommande, c : Commande) =>
  {
     CommandeComplet(d.orderid, d.productid, d.shipdate, d.billdate, d.unitprice, d.numunits, d.totalprice, c.city, c.state)
  },
    JoinWindows.of(Duration.ofMinutes(5))
  )

  kjoin.to("commandeComplet")(produced)


  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }
}
