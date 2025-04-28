package com.example.keepnote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.domain.repository.NoteRepository
import com.example.keepnote.domain.usecase.NoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewmodel @Inject constructor(
    val noteUseCase: NoteUseCase
) : ViewModel(){

    fun saveNote(note : NoteEntity){
        viewModelScope.launch {
            noteUseCase.saveNote(note)
        }
    }
    fun updateNote(note : NoteEntity){
        viewModelScope.launch {
            noteUseCase.updateNote(note)
        }
    }
    suspend fun getNote(noteId: Int): NoteEntity {
        return noteUseCase.getNote(noteId)
    }
    fun deleteNote(noteId : Int){
        viewModelScope.launch {
            noteUseCase.deleteNote(noteId)
        }
    }
}