package kz.nearbygems.cashcard.model

import org.springframework.data.annotation.Id

data class CashCard(@Id var id: Long? = null,
                    var amount: Double?,
                    var owner: String?)