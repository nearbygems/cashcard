package kz.nearbygems.cashcard.service.impl

import kz.nearbygems.cashcard.model.CashCard
import kz.nearbygems.cashcard.repository.CashCardRepository
import kz.nearbygems.cashcard.service.CashCardService
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class CashCardServiceImpl(val repository: CashCardRepository) : CashCardService {

  private val default: Sort = Sort.by(Sort.Direction.ASC, "amount")

  override fun findById(requestedId: Long, owner: String): CashCard =
      repository.findByIdAndOwner(requestedId, owner)
      ?: throw NotFoundException()

  override fun findAll(pageable: Pageable, owner: String): List<CashCard> =
      repository.findByOwner(owner,
                             PageRequest.of(pageable.pageNumber,
                                            pageable.pageSize,
                                            pageable.getSortOr(default))).content

  override fun createCashCard(newCashCardRequest: CashCard, ucb: UriComponentsBuilder, owner: String): URI {

    val cashCardWithOwner = CashCard(null,
                                     newCashCardRequest.amount,
                                     owner)

    val savedCashCard = repository.save(cashCardWithOwner)

    return ucb.path("cashcards/{id}")
        .buildAndExpand(savedCashCard.id)
        .toUri()
  }

  override fun putCashCard(requestedId: Long, cashCardUpdate: CashCard, owner: String) {
    repository.findByIdAndOwner(requestedId, owner)
        ?.let { repository.save(CashCard(it.id, cashCardUpdate.amount, owner)) }
    ?: throw NotFoundException()
  }

  override fun deleteCashCard(id: Long, owner: String) {
    if (repository.existsByIdAndOwner(id, owner)) {
      repository.deleteById(id)
    } else {
      throw NotFoundException()
    }
  }

}