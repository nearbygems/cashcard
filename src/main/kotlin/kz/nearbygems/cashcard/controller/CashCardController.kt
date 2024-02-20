package kz.nearbygems.cashcard.controller

import kz.nearbygems.cashcard.model.CashCard
import kz.nearbygems.cashcard.service.CashCardService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal

@RestController
@RequestMapping("/cashcards")
class CashCardController(val service: CashCardService) {

  @GetMapping("/{requestedId}")
  private fun findById(@PathVariable requestedId: Long,
                       principal: Principal): ResponseEntity<CashCard> =
      service.findById(requestedId, principal.name).let { ResponseEntity.ok(it) }

  @GetMapping
  private fun findAll(pageable: Pageable,
                      principal: Principal): ResponseEntity<List<CashCard>> =
      ResponseEntity.ok(service.findAll(pageable, principal.name))

  @PostMapping
  private fun createCashCard(@RequestBody newCashCardRequest: CashCard,
                             ucb: UriComponentsBuilder,
                             principal: Principal): ResponseEntity<Void> =
      ResponseEntity.created(service.createCashCard(newCashCardRequest, ucb, principal.name)).build()

  @PutMapping("/{requestedId}")
  private fun putCashCard(@PathVariable requestedId: Long,
                          @RequestBody cashCardUpdate: CashCard,
                          principal: Principal): ResponseEntity<Void> {
    service.putCashCard(requestedId, cashCardUpdate, principal.name)
    return ResponseEntity.noContent().build()
  }

  @DeleteMapping("/{id}")
  private fun deleteCashCard(@PathVariable id: Long,
                             principal: Principal): ResponseEntity<Void> {
    service.deleteCashCard(id, principal.name)
    return ResponseEntity.noContent().build()
  }

}