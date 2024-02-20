package kz.nearbygems.cashcard.controller


import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import kz.nearbygems.cashcard.model.CashCard
import kz.nearbygems.cashcard.service.CashCardService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import kotlin.test.Test

@WebMvcTest(CashCardController::class)
class CashCardControllerTest {

  @Autowired
  lateinit var mvc: MockMvc

  @MockkBean
  lateinit var service: CashCardService

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should return a cash card when data is saved`() {

    listOf("asd").filter { it.isBlank() }

    val cashCard = CashCard(99L, 123.45, "sarah")

    every {
      service.findById(cashCard.id!!, cashCard.owner!!)
    } returns cashCard

    //
    mvc.get("/cashcards/99").andExpectAll {
      status().isOk
      content { contentType(MediaType.APPLICATION_JSON) }
      jsonPath("$.id") { value(cashCard.id) }
      jsonPath("$.amount") { value(cashCard.amount) }
      jsonPath("$.owner") { value(cashCard.owner) }
    }
    //

    verify(exactly = 1) {
      service.findById(cashCard.id!!, cashCard.owner!!)
    }

  }

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should not return a cash card with an unknown id`() {

    every {
      service.findById(any(), any())
    } throws NotFoundException()

    //
    mvc.get("/cashcards/99").andExpect {
      status().isNotFound
    }
    //

    verify(exactly = 1) {
      service.findById(any(), any())
    }

  }

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should create a new cash card`() {

    val newCashCard = CashCard(null, 250.00, "sarah")

    val uri = URI.create("cashcards/99")

    every {
      service.createCashCard(newCashCard, any(), newCashCard.owner!!)
    } returns uri

    //
    mvc.post("/cashcards") {
      with(csrf())
      contentType = MediaType.APPLICATION_JSON
      content = jsonMapper().writeValueAsString(newCashCard)
    }.andExpectAll {
      status().isCreated
      header { string("location", uri.path) }
    }.andReturn().response
    //

    verify(exactly = 1) {
      service.createCashCard(newCashCard, any(), newCashCard.owner!!)
    }

  }

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should update an existing cash card`() {

    val cashCardUpdate = CashCard(null, 19.99, "sarah")

    val cashCardId = 99L

    justRun {
      service.putCashCard(cashCardId, cashCardUpdate, cashCardUpdate.owner!!)
    }

    //
    mvc.put("/cashcards/$cashCardId") {
      with(csrf())
      contentType = MediaType.APPLICATION_JSON
      content = jsonMapper().writeValueAsString(cashCardUpdate)
    }.andExpect {
      status().isOk
    }
    //

    verify(exactly = 1) {
      service.putCashCard(cashCardId, cashCardUpdate, cashCardUpdate.owner!!)
    }

  }

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should delete an existing cash card`() {

    val cashCardId = 99L

    justRun {
      service.deleteCashCard(cashCardId, "sarah")

    }

    //
    mvc.delete("/cashcards/$cashCardId") {
      with(csrf())
    }.andExpect {
      status().isOk
    }
    //

    verify(exactly = 1) {
      service.deleteCashCard(cashCardId, "sarah")
    }

  }

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should not update a cash card that does not exist`() {

    val cashCardUpdate = CashCard(null, 19.99, "sarah")

    val cashCardId = 99L

    every {
      service.putCashCard(cashCardId, cashCardUpdate, cashCardUpdate.owner!!)
    } throws NotFoundException()

    //
    mvc.put("/cashcards/$cashCardId") {
      with(csrf())
      contentType = MediaType.APPLICATION_JSON
      content = jsonMapper().writeValueAsString(cashCardUpdate)
    }.andExpect {
      status().isNotFound
    }
    //

    verify(exactly = 1) {
      service.putCashCard(cashCardId, cashCardUpdate, cashCardUpdate.owner!!)
    }

  }

  @Test
  @WithMockUser(username = "sarah", password = "abc123", roles = ["CARD-OWNER"])
  fun `should return all cash cards when list is requested`() {

    val firstCashCard = CashCard(99L, 123.45, null)
    val secondCashCard = CashCard(100L, 1.00, null)
    val thirdCashCard = CashCard(101L, 150.00, null)

    val cashCardList = listOf(firstCashCard, secondCashCard, thirdCashCard)

    every {
      service.findAll(any(), "sarah")
    } returns cashCardList

    //
    mvc.get("/cashcards").andExpectAll {
      status().isOk
      content { contentType(MediaType.APPLICATION_JSON) }
      jsonPath("$.length()") { value(cashCardList.size) }
      jsonPath("$..id") {
        arrayOf(firstCashCard.id, secondCashCard.id, thirdCashCard.id)
      }
      jsonPath("$..amount") {
        arrayOf(firstCashCard.amount, secondCashCard.amount, thirdCashCard.amount)
      }
    }
    //

    verify(exactly = 1) {
      service.findAll(any(), "sarah")
    }

  }

  @Test
  @WithMockUser(username = "BAD-USER", password = "abc123", roles = ["CARD-OWNER"])
  fun `should not return a cash card when using wrong username`() {

    every {
      service.findById(any(), any())
    } throws NotFoundException()

    //
    mvc.get("/cashcards/99").andExpect {
      status().isUnauthorized
    }
    //

  }

  @Test
  @WithMockUser(username = "BAD-USER", password = "abc123", roles = ["CARD-OWNER"])
  fun `should not return a cash card when using wrong password`() {

    every {
      service.findById(any(), any())
    } throws NotFoundException()

    //
    mvc.get("/cashcards/99").andExpect {
      status().isUnauthorized
    }
    //

  }

}