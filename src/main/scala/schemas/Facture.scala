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
