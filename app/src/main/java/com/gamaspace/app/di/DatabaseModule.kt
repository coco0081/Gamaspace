package com.gamaspace.app.di

import android.content.Context
import androidx.room.Room
import com.gamaspace.app.data.database.GamaspaceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para inyección de dependencias
 * Proporciona singletons para BD, DAO y Repository
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGamaspaceDatabase(
        @ApplicationContext context: Context
    ): GamaspaceDatabase {
        return Room.databaseBuilder(
            context,
            GamaspaceDatabase::class.java,
            "gamaspace_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAppDao(database: GamaspaceDatabase) = database.appDao()

    @Provides
    @Singleton
    fun providePerformanceStatsDao(database: GamaspaceDatabase) = 
        database.performanceStatsDao()

    @Provides
    @Singleton
    fun provideOptimizationProfileDao(database: GamaspaceDatabase) = 
        database.optimizationProfileDao()

    @Provides
    @Singleton
    fun provideGameHistoryDao(database: GamaspaceDatabase) = 
        database.gameHistoryDao()

    @Provides
    @Singleton
    fun provideCacheDataDao(database: GamaspaceDatabase) = 
        database.cacheDataDao()

    @Provides
    @Singleton
    fun provideNotificationDao(database: GamaspaceDatabase) = 
        database.notificationDao()
}
