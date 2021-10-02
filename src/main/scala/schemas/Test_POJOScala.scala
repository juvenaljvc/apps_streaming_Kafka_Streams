package schemas
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty, JsonPropertyOrder}
import org.apache.commons.lang.builder.ToStringBuilder
import scala.beans.BeanProperty


class Test_POJOScala {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonPropertyOrder(
      Array("AddressLine", "City", "State", "PinCode", "ContactNumber")
  )
  class DeliveryAddress {

    @JsonProperty("AddressLine")
    @BeanProperty
    var addressLine: String = _

    @JsonProperty("City")
    @BeanProperty
    var city: String = _

    @JsonProperty("State")
    @BeanProperty
    var state: String = _

    @JsonProperty("PinCode")
    @BeanProperty
    var pinCode: String = _

    @JsonProperty("ContactNumber")
    @BeanProperty
    var contactNumber: String = _

    def withAddressLine(addressLine: String): DeliveryAddress = {
      this.addressLine = addressLine
      this
    }

    def withCity(city: String): DeliveryAddress = {
      this.city = city
      this
    }

    def withState(state: String): DeliveryAddress = {
      this.state = state
      this
    }

    def withPinCode(pinCode: String): DeliveryAddress = {
      this.pinCode = pinCode
      this
    }

    def withContactNumber(contactNumber: String): DeliveryAddress = {
      this.contactNumber = contactNumber
      this
    }

    override def toString(): String =
      new ToStringBuilder(this)
        .append("addressLine", addressLine)
        .append("city", city)
        .append("state", state)
        .append("pinCode", pinCode)
        .append("contactNumber", contactNumber)
        .toString
  }


}
