//package com.virtua.cycles.service
//
//import com.virtua.cycles.model.User
//import com.virtua.cycles.repository.UserRepository
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.stereotype.Service
//
//@Service
//class UserService(@Autowired private val userRepository: UserRepository) {
//
//    fun getAllUsers(): List<User> = userRepository.findAll()
//
//    fun getUserById(id: String): User? = userRepository.findById(id).orElse(null)
//
//    fun getUserByEmail(email: String): User? = userRepositoryository.findByEmail(email)
//
//    fun createUser(user: User): User = userRepository.save(user)
//
//    fun updateUser(id: String, newUserData: User): User? {
//        val existingUser = userRepository.findById(id)
//        return if (existingUser.isPresent) {
//            val userToUpdate = existingUser.get().copy(
//                name = newUserData.name,
//                email = newUserData.email,
//                age = newUserData.age
//            )
//            userRepository.save(userToUpdate)
//        } else null
//    }
//
//    fun deleteUser(id: String): Boolean {
//        return if (userRepository.existsById(id)) {
//            userRepository.deleteById(id)
//            true
//        } else false
//    }
//}