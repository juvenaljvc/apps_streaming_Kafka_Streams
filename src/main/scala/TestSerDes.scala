import com.fasterxml.jackson.annotation.JsonInclude
import schemas.{Facture, OrderLine}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object TestSerDes extends App {

  val facture1  = Facture("a320", "téléviseur LG 3A Nano", 3, 3350.75, OrderLine("34e", "45i", "20/09/2010", "20/09/2010", 15.00, 700, 10))
  println(facture1.orderline.totalprice)

  // Sérialisation/Désérialisation des objets en scala avec Jackson
  val objetMapper : ObjectMapper = new ObjectMapper()
  objetMapper.registerModule(DefaultScalaModule)
  objetMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  objetMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
  objetMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
  objetMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, true)
  objetMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  val s = objetMapper.writeValueAsString(facture1)   // sérialisation en format json
  println("sérialisation de facture : " + s)

  val sb = objetMapper.writeValueAsBytes(facture1)   // sérialisation en format binaire
  println("sérialisation de facture : " + sb)

  val d = objetMapper.readValue(s, classOf[Facture])   // désérialisation
  val db = objetMapper.readValue(sb, classOf[Facture])

  println(db)


}
