package com.example.keepnote.domain.repository

import com.example.keepnote.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<NoteEntity>>
    suspend fun refreshNotesFromApi()
}
