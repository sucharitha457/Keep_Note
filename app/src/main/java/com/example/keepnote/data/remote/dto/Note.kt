package com.example.keepnote.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NoteDto(
    val id: String,
    val archived: Boolean,
    val title: String,
    val body: String,
    val created_time: Long,
    val image: String?
)

