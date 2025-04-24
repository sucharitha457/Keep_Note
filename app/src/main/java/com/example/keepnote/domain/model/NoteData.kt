package com.example.keepnote.domain.model

data class NoteData(
    val id: String?,
    val archived: Boolean,
    val title: String,
    val body: String,
    val createdTime: Long,
    val image: String?
)
