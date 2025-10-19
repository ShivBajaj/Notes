package com.sb.notes.controllers

import com.sb.notes.database.model.Note
import com.sb.notes.database.repository.NotesRepository
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    val notesRepository: NotesRepository
) {

    data class NoteRequest(
        val title: String,
        val content: String,
        val color: Long
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping
    fun save(
        @RequestBody request: NoteRequest
    ): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val note = notesRepository.save(
            Note(
                title = request.title,
                content = request.content,
                color = request.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )
        return note.toNoteResponse()
    }

    @GetMapping
    fun getByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return notesRepository.findByOwnerId(ObjectId(ownerId)).map { it.toNoteResponse() }
    }

    @DeleteMapping("/{id}")
    fun deleteByOwnerId(
        @PathVariable id: String
    ){
        val note = notesRepository.findById(ObjectId(id))
            .orElseThrow { throw Exception("Note not found") }

        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if(note.ownerId.toHexString() == ownerId){
            notesRepository.deleteById(ObjectId(id))
        }
    }

    private fun Note.toNoteResponse(): NoteResponse {
        return NoteResponse(
            id = ObjectId().toHexString(),
            title = title,
            content = content,
            color = color,
            createdAt = createdAt
        )
    }
}