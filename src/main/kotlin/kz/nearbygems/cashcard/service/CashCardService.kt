package kz.nearbygems.cashcard.service

import kz.nearbygems.cashcard.model.CashCard
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

interface CashCardService {

  fun findById(requestedId: Long,
               owner: String): CashCard

  fun findAll(pageable: Pageable,
              owner: String): List<CashCard>

  fun createCashCard(newCashCardRequest: CashCard,
                     ucb: UriComponentsBuilder,
                     owner: String): URI

  fun putCashCard(requestedId: Long,
                  cashCardUpdate: CashCard,
                  owner: String)

  fun deleteCashCard(id: Long,
                     owner: String)

}