package com.example.keepnote.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepnote.data.local.AppDatabase
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewmodel @Inject constructor(
    val repository: NoteRepository
) : ViewModel(){
    fun saveNote(note : NoteEntity){
        viewModelScope.launch {
            repository.saveNote(note)
        }
    }
    fun updateNote(note : NoteEntity){
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }
    suspend fun getNote(noteId: Int): NoteEntity {
        return repository.getNote(noteId)
    }
    fun deleteNote(noteId : Int){
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }
}