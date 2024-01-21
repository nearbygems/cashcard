package kz.nearbygems.cashcard

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import kotlin.test.Test

@JsonTest
class CashCardJsonTest {

  @Autowired
  private lateinit var json: JacksonTester<CashCard>

  @Autowired
  private lateinit var jsonList: JacksonTester<Array<CashCard>>

  private lateinit var cashCards: Array<CashCard>

  @BeforeEach
  fun setUp() {
    cashCards = arrayOf(CashCard(99L, 123.45, "sarah"),
                        CashCard(100L, 1.00, "sarah"),
                        CashCard(101L, 150.00, "sarah"))
  }

  @Test
  fun `cash card serialization`() {
    val cashCard = cashCards[0]
    assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json")
    assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id")
    assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99)
    assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount")
    assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45)
  }

  @Test
  fun `cash card deserialization`() {
    val expected = """
      {
      "id": 99,
      "amount": 123.45,
      "owner": "sarah"
      }
      """
    assertThat(json.parse(expected)).isEqualTo(CashCard(99L, 123.45, "sarah"))
    assertThat(json.parseObject(expected).id).isEqualTo(99)
    assertThat(json.parseObject(expected).amount).isEqualTo(123.45)
    assertThat(json.parseObject(expected).owner).isEqualTo("sarah")
  }

  @Test
  fun `cash card list serialization`() {
    assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json")
  }

  @Test
  fun `cash card list deserialization`() {
    val expected = """
    [
    { "id": 99, "amount": 123.45, "owner": "sarah" },
    { "id": 100, "amount": 1.00, "owner": "sarah" },
    { "id": 101, "amount": 150.00, "owner": "sarah" }
    ]
    """
    assertThat(jsonList.parse(expected)).isEqualTo(cashCards)
  }

}