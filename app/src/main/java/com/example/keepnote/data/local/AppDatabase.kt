package com.example.keepnote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 10, exportSchema = false)
abstract class AppDatabase :RoomDatabase(){
    abstract fun noteDao() : NoteDao
}