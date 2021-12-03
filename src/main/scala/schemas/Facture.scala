package schemas
import java.util.Date

case class Facture(
    factureid :  String,
    productName : String,
    qantite : Integer,
    total : Double,
    orderline : OrderLine
    )

case class OrderLine(
    orderlineid : String,
    productid : String,
    shipdate : String,
    billdate : String,
    unitprice : Double,
    totalprice : Double,
    numunits : Int
    )

case class DetailsCommande(
    orderid : Long,
    productid : Long,
    shipdate : String,
    billdate : String,
    unitprice : Double,
    numunits : Int,
    totalprice : Double
 )

case class Commande(
     customerid : Long,
     campaignid : Long,
     orderdate : String,
     city : String,
     state : String,
     paymenttype : String
)

case class CommandeComplet(
      orderid : Long,
      productid : Long,
      shipdate : String,
      billdate : String,
      unitprice : Double,
      numunits : Int,
      totalprice : Double,
      city : String,
      state : String
)