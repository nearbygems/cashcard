package kz.nearbygems.cashcard

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal

@RestController
@RequestMapping("/cashcards")
class CashCardController(val repository: CashCardRepository) {

  private val default: Sort = Sort.by(Sort.Direction.ASC, "amount")

  @GetMapping("/{requestedId}")
  private fun findById(@PathVariable requestedId: Long,
                       principal: Principal): ResponseEntity<CashCard> {

    val cashCard: CashCard? = repository.findByIdAndOwner(requestedId, principal.name)

    return if (cashCard != null) ResponseEntity.ok(cashCard)
    else ResponseEntity.notFound().build()
  }

  @GetMapping
  private fun findAll(pageable: Pageable,
                      principal: Principal): ResponseEntity<List<CashCard>> =
          ResponseEntity.ok(repository.findByOwner(principal.name,
                                                   PageRequest.of(pageable.pageNumber,
                                                                  pageable.pageSize,
                                                                  pageable.getSortOr(default)))
                                    .content)

  @PostMapping
  private fun createCashCard(@RequestBody newCashCardRequest: CashCard,
                             ucb: UriComponentsBuilder,
                             principal: Principal): ResponseEntity<Void> {

    val cashCardWithOwner = CashCard(null,
                                     newCashCardRequest.amount,
                                     principal.name)

    val savedCashCard = repository.save(cashCardWithOwner)

    val locationOfNewCashCard = ucb.path("cashcards/{id}")
            .buildAndExpand(savedCashCard.id)
            .toUri()

    return ResponseEntity.created(locationOfNewCashCard).build()
  }

  @PutMapping("/{requestedId}")
  private fun putCashCard(@PathVariable requestedId: Long,
                          @RequestBody cashCardUpdate: CashCard,
                          principal: Principal): ResponseEntity<Void> {

    val cashCard: CashCard? = repository.findByIdAndOwner(requestedId, principal.name)

    if (cashCard != null) {

      val updatedCashCard = CashCard(cashCard.id,
                                     cashCardUpdate.amount,
                                     principal.name)

      repository.save(updatedCashCard)

      return ResponseEntity.noContent().build()
    }

    return ResponseEntity.notFound().build()
  }

  @DeleteMapping("/{id}")
  private fun deleteCashCard(@PathVariable id: Long,
                             principal: Principal): ResponseEntity<Void> {

    if (repository.existsByIdAndOwner(id, principal.name)) {
      repository.deleteById(id)
      return ResponseEntity.noContent().build()
    }

    return ResponseEntity.notFound().build()
  }

}