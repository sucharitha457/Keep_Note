package com.example.keepnote.data.remote.api

import com.example.keepnote.data.remote.dto.NoteDto
import retrofit2.http.GET

interface ApiSerivce {
    @GET("notes")
    suspend fun getNotes(): List<NoteDto>
}
