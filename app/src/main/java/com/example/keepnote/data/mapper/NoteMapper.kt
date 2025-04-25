package com.example.keepnote.data.mapper

import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.data.remote.dto.NoteDto
import com.example.keepnote.domain.model.NoteData

fun NoteDto.toDomain(): NoteData {
    return NoteData(
        id = id,
        archived = archived,
        title = title,
        body = body,
        createdTime = created_time,
        image = image
    )
}


fun NoteDto.toEntity(): NoteEntity {
    return NoteEntity(
        noteId = id,
        archived = archived,
        title = title,
        body = body,
        createdTime = created_time,
        image = image,
        isApiData = true
    )
}
