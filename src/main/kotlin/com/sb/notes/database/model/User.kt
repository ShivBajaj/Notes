package com.sb.notes.database.model

import jakarta.validation.constraints.Email
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    @Indexed(unique = true)
    val email: String,
    val hashedPassword: String,
    @Id val id : ObjectId = ObjectId.get()
)
