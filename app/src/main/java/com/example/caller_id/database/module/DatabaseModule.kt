package com.example.caller_id.database.module

import android.content.Context
import androidx.room.Room
import com.example.caller_id.database.dao.BlockedNumberDao
import com.example.caller_id.database.dao.BlockedSmsDao
import com.example.caller_id.database.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context) =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "sms_db").build()
    @Provides fun provideNumDao(db: AppDatabase) = db.blockedNumberDao()
    @Provides fun provideSmsDao(db: AppDatabase) = db.blockedSmsDao()
    @Provides fun provideCalledDao(db: AppDatabase) = db.blockedCalledDao()
}