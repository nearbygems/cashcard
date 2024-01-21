package kz.nearbygems.cashcard

import org.springframework.data.annotation.Id

data class CashCard(@Id var id: Long? = null,
                    var amount: Double?,
                    var owner: String?)