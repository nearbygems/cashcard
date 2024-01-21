package kz.nearbygems.cashcard

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class CashCardApplication

fun main(args: Array<String>) {
  runApplication<CashCardApplication>(*args) {
    setBannerMode(Banner.Mode.OFF)
  }
}