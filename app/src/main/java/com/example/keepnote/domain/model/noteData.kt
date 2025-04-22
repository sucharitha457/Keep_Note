package com.example.keepnote.domain.model

data class noteData(
    val id: String,
    val archived: Boolean,
    val title: String,
    val body: String,
    val created_time: Long,
    val image: String?
)
