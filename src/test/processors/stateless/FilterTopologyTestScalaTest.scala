package processors.stateless

import org.apache.kafka.common.serialization.{DoubleDeserializer, IntegerDeserializer, IntegerSerializer, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import schemas.{Facture, OrderLine}
import serdes.{JSONDeserializer, JSONSerializer}

import java.util.Properties


class FilterTopologyTestScalaTest extends AnyFlatSpec {
  val stringSes = new StringSerializer
  val factureSes = new JSONSerializer[Facture]

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-processor-test")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val filterProcessorTopology =  FiltersProcessors
  val testDriver = new TopologyTestDriver(filterProcessorTopology.topologyBuilder(),props)

  "le nombre d'article de la facture" should("être égal à 15 :") in {
    val facture1  = Facture("a326", "téléviseur LG 3A Nano", 3, 3350.85, OrderLine("34e", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10))
    val factureFactory  : ConsumerRecordFactory[String, Facture]  = new ConsumerRecordFactory("factureJson", stringSes, factureSes)
    testDriver .pipeInput(factureFactory.create("factureJson", facture1.factureid, facture1))

    // lecture du record
    val facture = testDriver.readOutput("topic-test", new StringDeserializer, new DoubleDeserializer)

    assert(facture.key() == "a326")
  }

}
