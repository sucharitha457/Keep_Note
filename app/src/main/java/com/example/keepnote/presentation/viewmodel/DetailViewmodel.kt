package com.example.keepnote.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepnote.data.local.AppDatabase
import com.example.keepnote.data.local.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewmodel @Inject constructor(
    private val database: AppDatabase
) : ViewModel(){

    fun saveNote(note : NoteEntity){
        viewModelScope.launch {
            database.noteDao().insertNote(note)
            Log.d( "detail screen", "saveNote: ${note.title}")
        }
    }

    fun updateNote(note : NoteEntity){
        viewModelScope.launch {
            database.noteDao().updateNote(note)
        }
    }

    suspend fun getNote(noteId: Int): NoteEntity {
        return database.noteDao().getNoteById(noteId)
    }

    fun deleteNote(noteId : Int){
        viewModelScope.launch {
            database.noteDao().deleteNote(noteId)
        }
    }
}