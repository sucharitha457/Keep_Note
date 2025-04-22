package com.example.keepnote.data.repository

import com.example.keepnote.data.local.NoteDao
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.data.mapper.toDomain
import com.example.keepnote.data.mapper.toEntity
import com.example.keepnote.data.remote.api.ApiSerivce
import com.example.keepnote.domain.model.noteData
import com.example.keepnote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val apiService: ApiSerivce,
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotes(): Flow<List<NoteEntity>> {
        return noteDao.getNotes()
    }

    override suspend fun refreshNotesFromApi() {
        try {
            val notesFromApi = apiService.getNotes()
            val noteEntities = notesFromApi.map { it.toEntity() }
            noteDao.insertNotes(noteEntities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
