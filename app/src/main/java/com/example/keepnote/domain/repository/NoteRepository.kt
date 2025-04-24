package com.example.keepnote.domain.repository

import com.example.keepnote.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<NoteEntity>>
    suspend fun refreshNotesFromApi()
    suspend fun saveNote(note : NoteEntity)
    suspend fun updateNote(note : NoteEntity)
    suspend fun getNote(noteId: Int): NoteEntity
    suspend fun deleteNote(noteId : Int)
}
