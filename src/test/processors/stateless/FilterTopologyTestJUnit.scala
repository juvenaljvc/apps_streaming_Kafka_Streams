package processors.stateless
import org.junit.jupiter.api.{AfterAll, BeforeAll, DisplayName, MethodOrderer, Order, Test, TestMethodOrder}
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}
import org.apache.kafka.streams.test.{ConsumerRecordFactory, OutputVerifier}
import org.apache.kafka.common.serialization
import org.apache.kafka.common.serialization.{DoubleDeserializer, DoubleSerializer, Serde, Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.scala.Serdes._
import schemas.{Facture, OrderLine}
import serdes.{JSONDeserializer, JSONSerializer}
import org.junit.Assert.{assertEquals, assertNotEquals}
import org.junit.{After, Before}

import java.util.Properties

@TestMethodOrder(classOf[MethodOrderer.OrderAnnotation])
class FilterTopologyTestJUnit {

  val stringSes = new StringSerializer
  val factureSes = new JSONSerializer[Facture]

  val props : Properties = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-processor-test")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val filterProcessorTopology =  FiltersProcessors
  val testDriver = new TopologyTestDriver(filterProcessorTopology.topologyBuilder(),props)


  @Before
  def setUpAll(): Unit = {
    testDriver

  }

  @Test
  @Order(1)
  @DisplayName("nom de mon test 1")
  def methodeTest1() : Unit = {

    val facture1  = Facture("a326", "téléviseur LG 3A Nano", 3, 3350.85, OrderLine("34e", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10))
    val factureFactory  : ConsumerRecordFactory[String, Facture]  = new ConsumerRecordFactory("factureJson", stringSes, factureSes)
    testDriver .pipeInput(factureFactory.create("factureJson", facture1.factureid, facture1))

    // lecture du record
    val facture = testDriver.readOutput("topic-test", new StringDeserializer, new DoubleDeserializer)
    OutputVerifier.compareValue(facture,facture.value())

    assertEquals("le chiffre d'affaire de la facture doit être égal à :", 150, facture.value().toInt)

  }

  @Test
  @Order(2)
  @DisplayName("nom de mon test 2")
  def methodeTest2() : Unit = {
    val facture1  = Facture("a326", "téléviseur LG 3A Nano", 3, 3350.85, OrderLine("34e", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10))
    val factureFactory   : ConsumerRecordFactory[String, Facture] = new ConsumerRecordFactory("factureJson", stringSes, factureSes)
    testDriver .pipeInput(factureFactory.create(facture1))

    // lecture du record
    val facture = testDriver.readOutput("topic-test", new StringDeserializer, new DoubleDeserializer)

    assertEquals("le nombre d'article de la facture doit être égal à :", 150, facture.value())
    assert(150< facture.value())
    testDriver.close()
  }

  @After
  def cleanupAll() : Unit = {
    testDriver.close()
  }

}
