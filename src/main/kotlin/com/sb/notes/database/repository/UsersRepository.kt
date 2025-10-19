package com.sb.notes.database.repository

import com.sb.notes.database.model.User
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UsersRepository: MongoRepository<User, ObjectId> {
    fun findByEmail(email: String): User?
}