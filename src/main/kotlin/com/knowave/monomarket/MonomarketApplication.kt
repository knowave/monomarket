package com.knowave.monomarket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class MonomarketApplication

fun main(args: Array<String>) {
	runApplication<MonomarketApplication>(*args)
}
