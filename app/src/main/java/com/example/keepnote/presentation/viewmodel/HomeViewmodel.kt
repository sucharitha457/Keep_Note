package com.example.keepnote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepnote.data.local.AppDatabase
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val database: AppDatabase
) : ViewModel() {

    fun getNotes(): Flow<List<NoteEntity>> = noteRepository.getNotes()

    fun refreshFromApi() {
        viewModelScope.launch {
            noteRepository.refreshNotesFromApi()
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
}
