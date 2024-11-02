package com.example.keepmynotes.di

import com.example.keepmynotes.data.repository.AuthRepository
import com.example.keepmynotes.data.repository.FirebaseDbRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideFirebaseDbRepository(): FirebaseDbRepository {
        return FirebaseDbRepository()
    }
}