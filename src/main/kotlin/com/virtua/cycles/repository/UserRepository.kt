package com.virtua.cycles.repository

import com.virtua.cycles.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface UserRepository : MongoRepository<User, String>{
    fun findByEmail(email: String): User?//implementar automáticamente buscar usuario por su mail
    fun findByName(name: String): User? //buscar usuario por su 'name' para encontrar su username


}