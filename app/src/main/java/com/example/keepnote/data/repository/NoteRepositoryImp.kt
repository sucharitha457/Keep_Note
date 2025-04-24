package com.example.keepnote.data.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.keepnote.data.local.NoteDao
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.data.mapper.toEntity
import com.example.keepnote.data.remote.api.ApiSerivce
import com.example.keepnote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val apiService: ApiSerivce,
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotes(): Flow<List<NoteEntity>> {
        return noteDao.getNotes()
            .onEach { notes ->
                Log.d("NoteRepository", "Notes emitted: $notes")
            }
    }
    override suspend fun saveNote(note : NoteEntity){
        noteDao.insertNote(note)
    }

    override suspend fun updateNote(note : NoteEntity){
        noteDao.updateNote(note)
    }

    override suspend fun getNote(noteId: Int): NoteEntity {
        return noteDao.getNoteById(noteId)
    }

    override suspend fun deleteNote(noteId : Int){
        noteDao.deleteNote(noteId)
    }

    override suspend fun refreshNotesFromApi() {
        try {
            val notesFromApi = apiService.getNotes()
            notesFromApi.forEach { noteDto ->
                val noteEntity = noteDto.toEntity()
                if (noteDao.checkIfNoteExists(noteEntity.noteId)) {
                    updateNote(noteEntity)
                } else {
                    noteDao.insertNote(noteEntity)
                }
            }
        } catch (e: Exception) {
            Log.e("NoteRepo", "Error refreshing notes: ${e.message}", e)
        }
    }
}
