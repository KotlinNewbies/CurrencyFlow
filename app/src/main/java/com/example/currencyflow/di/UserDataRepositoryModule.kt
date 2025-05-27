package com.example.currencyflow.di


import com.example.currencyflow.data.repository.FileUserDataRepository
import com.example.currencyflow.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserDataRepositoryModule { // Możesz to dodać do istniejącego RepositoryModule

    @Binds
    @Singleton
    abstract fun bindUserDataRepository(
        fileUserDataRepository: FileUserDataRepository
    ): UserDataRepository
}