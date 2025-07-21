package com.virtua.cycles


import io.github.cdimascio.dotenv.dotenv
//import io.jsonwebtoken.security.Keys
//import java.util.Base64
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CyclesApplication

fun main(args: Array<String>) {
	runApplication<CyclesApplication>(*args)
	val dotenv = dotenv()
	System.setProperty("MONGODB_URI", dotenv["MONGODB_URI"])
//	val key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)
//	println(Base64.getEncoder().encodeToString(key.encoded))
}


