package com.example.keepnote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: String,
    val archived: Boolean,
    val title: String,
    val body: String,
    val created_time: Long,
    val image: String?
)