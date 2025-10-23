package com.sb.notes.database.repository

import com.sb.notes.database.model.User
import io.jsonwebtoken.security.Password
import jakarta.validation.constraints.Email
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UsersRepository: MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?
}