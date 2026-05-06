package com.fibcam.di

import android.content.Context
import com.fibcam.data.AppPreferences
import com.fibcam.sensor.AccelerometerHelper
import com.fibcam.camera.ManualControlHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module providing app-level singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): AppPreferences = AppPreferences(context)

    @Provides
    @Singleton
    fun provideAccelerometerHelper(
        @ApplicationContext context: Context
    ): AccelerometerHelper = AccelerometerHelper(context)

    @Provides
    @Singleton
    fun provideManualControlHelper(): ManualControlHelper = ManualControlHelper()
}