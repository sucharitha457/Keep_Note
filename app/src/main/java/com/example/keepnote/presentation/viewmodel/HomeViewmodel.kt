package com.example.keepnote.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepnote.data.local.AppDatabase
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.data.remote.dto.NoteDto
import com.example.keepnote.domain.model.noteData
import com.example.keepnote.domain.repository.NoteRepository
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        getAllNotes()
    }

    private fun getAllNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                noteRepository.getNotes().collect { fetchedNotes ->
                    _notes.value = fetchedNotes
                    _isLoading.value = false
                }
                Log.d("NoteViewModel", "Fetched notes: ${_notes.value}")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("NoteViewModel", "Error fetching notes", e)
            } finally {
                _isLoading.value = false
                Log.d("NoteViewModel - final", "Fetched notes: ${_notes.value}")
            }
        }
    }
}
