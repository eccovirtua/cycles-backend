package com.virtua.cycles


import io.github.cdimascio.dotenv.dotenv

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CyclesApplication

fun main(args: Array<String>) {
	runApplication<CyclesApplication>(*args)
	val dotenv = dotenv()
	System.setProperty("MONGODB_URI", dotenv["MONGODB_URI"])

}


