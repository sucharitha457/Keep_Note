package com.example.keepnote.data.remote.api

import com.example.keepnote.data.remote.dto.NoteDto
import com.example.keepnote.domain.model.noteData
import retrofit2.http.GET

interface ApiSerivce {
    @GET("notes")
    suspend fun getNotes(): List<NoteDto>
}
