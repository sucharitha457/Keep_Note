package com.example.keepnote.domain.usecase

import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.domain.repository.NoteRepository
import javax.inject.Inject

class NoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend fun saveNote(note: NoteEntity) {
        repository.saveNote(note)
    }

    suspend fun updateNote(note: NoteEntity) {
        repository.updateNote(note)
    }

    suspend fun getNote(noteId: Int): NoteEntity {
        return repository.getNote(noteId)
    }

    suspend fun deleteNote(noteId: Int) {
        repository.deleteNote(noteId)
    }

    fun getNotes() = repository.getNotes()

    suspend fun refreshNotesFromApi() {
        repository.refreshNotesFromApi()
    }
}
