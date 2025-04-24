package com.example.keepnote.data.mapper

import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.data.remote.dto.NoteDto
import com.example.keepnote.domain.model.noteData

fun NoteDto.toDomain(): noteData {
    return noteData(
        id = id,
        archived = archived,
        title = title,
        body = body,
        created_time = created_time,
        image = image
    )
}


fun NoteDto.toEntity(): NoteEntity {
    return NoteEntity(
        noteId = id,
        archived = archived,
        title = title,
        body = body,
        created_time = created_time,
        image = image,
        isApiData = true
    )
}
