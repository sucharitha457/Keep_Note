package com.example.keepnote.domain.repository

import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.domain.model.noteData
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun getNotes(): Flow<List<NoteEntity>>
}