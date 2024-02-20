package kz.nearbygems.cashcard.repository

import kz.nearbygems.cashcard.model.CashCard
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface CashCardRepository : CrudRepository<CashCard, Long>,
                               PagingAndSortingRepository<CashCard, Long> {

  fun findByIdAndOwner(id: Long, owner: String): CashCard?

  fun findByOwner(owner: String, pageRequest: PageRequest): Page<CashCard>

  fun existsByIdAndOwner(id: Long, owner: String): Boolean

}