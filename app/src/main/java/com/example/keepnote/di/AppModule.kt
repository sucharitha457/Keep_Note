package com.example.keepnote.di

import com.example.keepnote.data.repository.NoteRepositoryImpl
import com.example.keepnote.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindArticleRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository
}
