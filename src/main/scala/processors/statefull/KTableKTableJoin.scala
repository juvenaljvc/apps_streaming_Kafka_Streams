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

// jointure KTable-KTable
object KTableKTableJoin extends App  {
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  implicit val jsonSerdesCommandes : Serde[Commande] = Serdes.serdeFrom(new JSONSerializer[Commande], new JSONDeserializerCommandes)
  implicit val jsonSerdesDetailsCommandes : Serde[DetailsCommande] = Serdes.serdeFrom(new JSONSerializer[DetailsCommande], new JSONDeserializerDtlCommandes)
  implicit val jsonSerdesCommandesComplet : Serde[CommandeComplet] = Serdes.serdeFrom(new JSONSerializer[CommandeComplet], new JSONDeserializerCmdComplet)

  implicit val consumedCommandes : Consumed[String, Commande] = Consumed.`with`(String, jsonSerdesCommandes)
  implicit val consumedDetailsCommandes: Consumed[String, DetailsCommande] = Consumed.`with`(String, jsonSerdesDetailsCommandes)

  implicit val produced : Produced[String, CommandeComplet] = Produced.`with`(String, jsonSerdesCommandesComplet)

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ktable-ktable-join")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val str : StreamsBuilder = new StreamsBuilder
  val ktCommande : KTable[String, Commande]  = str.table[String, Commande]("commande")(consumedCommandes)
  val ktDtlCommande : KTable[String, DetailsCommande]  = str.table[String, DetailsCommande]("DetailsCommande")(consumedDetailsCommandes)

  val ktjoin = ktDtlCommande.join(ktCommande)(
    (d : DetailsCommande, c : Commande) =>
    {
      CommandeComplet(d.orderid, d.productid, d.shipdate, d.billdate, d.unitprice, d.numunits, d.totalprice, c.city, c.state)
    }
  )

  ktjoin.toStream.to("commandeComplet")(produced)

  val topologie : Topology = str.build()
  val kkStream : KafkaStreams = new KafkaStreams(topologie, props)
  kkStream.start()

  sys.ShutdownHookThread {
    kkStream.close()
  }

}