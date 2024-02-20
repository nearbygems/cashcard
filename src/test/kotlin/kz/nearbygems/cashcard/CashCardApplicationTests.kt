package kz.nearbygems.cashcard

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import kz.nearbygems.cashcard.model.CashCard
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import java.net.URI
import kotlin.test.Test

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {

  @Autowired
  lateinit var restTemplate: TestRestTemplate

  @Test
  fun `should return a cash card when data is saved`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards/99", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val id: Number = documentContext.read("$.id")
    val amount: Double = documentContext.read("$.amount")
    val owner: String = documentContext.read("$.owner")

    assertThat(id).isEqualTo(99)
    assertThat(amount).isEqualTo(123.45)
    assertThat(owner).isEqualTo("sarah")
  }

  @Test
  fun `should not return a cash card with an unknown id`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards/1000", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(response.body).isBlank()
  }

  @Test
  @DirtiesContext
  fun `should create a new cash card`() {

    val newCashCard = CashCard(null, 250.00, "sarah")

    val createResponse: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .postForEntity("/cashcards", newCashCard, Void::class.java)

    assertThat(createResponse.statusCode).isEqualTo(HttpStatus.CREATED)

    val locationOfNewCashCard: URI? = createResponse.headers.getLocation()

    val getResponse: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity(locationOfNewCashCard, String::class.java)

    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(getResponse.body)
    val id: Number = documentContext.read("$.id")
    val amount: Double = documentContext.read("$.amount")
    val owner: String = documentContext.read("$.owner")

    assertThat(id).isNotNull()
    assertThat(amount).isEqualTo(250.00)
    assertThat(owner).isEqualTo("sarah")
  }

  @Test
  @DirtiesContext
  fun `should update an existing cash card`() {

    val cashCardUpdate = CashCard(null, 19.99, null)

    val request: HttpEntity<CashCard> = HttpEntity<CashCard>(cashCardUpdate)

    val response: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .exchange("/cashcards/99", HttpMethod.PUT, request, Void::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

    val getResponse: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards/99", String::class.java)

    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(getResponse.body)
    val id: Number = documentContext.read("$.id")
    val amount: Double = documentContext.read("$.amount")
    val owner: String = documentContext.read("$.owner")

    assertThat(id).isEqualTo(99)
    assertThat(amount).isEqualTo(19.99)
    assertThat(owner).isEqualTo("sarah")
  }

  @Test
  @DirtiesContext
  fun `should delete an existing cash card`() {

    val response: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .exchange("/cashcards/99", HttpMethod.DELETE, null, Void::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

    val getResponse: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards/99", String::class.java)

    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should not update a cash card that does not exist`() {

    val unknownCard = CashCard(null, 19.99, null)

    val request: HttpEntity<CashCard> = HttpEntity<CashCard>(unknownCard)

    val response: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .exchange("/cashcards/99999", HttpMethod.PUT, request, Void::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should return all cash cards when list is requested`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val cashCardCount: Int = documentContext.read("$.length()")
    val ids: JSONArray = documentContext.read("$..id")
    val amounts: JSONArray = documentContext.read("$..amount")

    assertThat(cashCardCount).isEqualTo(3)
    assertThat(ids).containsExactlyInAnyOrder(99, 100, 101)
    assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00)
  }

  @Test
  fun `should return a page of cash cards`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards?page=0&size=1", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val page: JSONArray = documentContext.read("$[*]")
    assertThat(page.size).isEqualTo(1)
  }

  @Test
  fun `should return a sorted page of cash cards`() {

    val uri = "/cashcards?page=0&size=1&sort=amount,desc"

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity(uri, String::class.java)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val read: JSONArray = documentContext.read("$[*]")
    val amount: Double = documentContext.read("$[0].amount")

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(read.size).isEqualTo(1)
    assertThat(amount).isEqualTo(150.00)
  }

  @Test
  fun `should return a sorted page of cash cards with no parameters and use default values`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards", String::class.javaObjectType)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val page: JSONArray = documentContext.read("$[*]")
    assertThat(page.size).isEqualTo(3)

    val amounts: JSONArray = documentContext.read("$..amount")
    assertThat(amounts).containsExactly(1.00, 123.45, 150.00)
  }

  @Test
  fun `should not return a cash card when using bad credentials`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("BAD-USER", "abc123")
        .getForEntity("/cashcards/99", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

    val newResponse: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "BAD-PASSWORD")
        .getForEntity("/cashcards/99", String::class.java)

    assertThat(newResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun `should not allow accessToCashCardsTheyDoNotOwn`() {

    val response: ResponseEntity<String> = restTemplate.withBasicAuth("sarah", "abc123")
        .getForEntity("/cashcards/102", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should not update a cash card that is owned by someone else`() {

    val kumarsCard = CashCard(null, 333.33, null)

    val request: HttpEntity<CashCard> = HttpEntity<CashCard>(kumarsCard)

    val response: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .exchange("/cashcards/102", HttpMethod.PUT, request, Void::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should not delete a cash card that does not exist`() {

    val deleteResponse: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .exchange("/cashcards/99999", HttpMethod.DELETE, null, Void::class.java)

    assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should not allow deletion of cash cards they do not own`() {

    val deleteResponse: ResponseEntity<Void> = restTemplate.withBasicAuth("sarah", "abc123")
        .exchange("/cashcards/102", HttpMethod.DELETE, null, Void::class.java)

    assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

    val getResponse: ResponseEntity<String> = restTemplate.withBasicAuth("kumar2", "xyz789")
        .getForEntity("/cashcards/102", String::class.java)

    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
  }

}