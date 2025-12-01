package com.duolingo.clone.language_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LanguageBackendApplication

fun main(args: Array<String>) {
	runApplication<LanguageBackendApplication>(*args)
}
