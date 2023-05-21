package com.kixfobby.security.quickresponse.room.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.kixfobby.security.quickresponse.room.database.AppDatabase
import com.kixfobby.security.quickresponse.service.worker.WorkExecutor
import com.kixfobby.security.quickresponse.billing.BillingManager
import com.kixfobby.security.quickresponse.helper.network.NetworkManager
import dagger.Module
import dagger.Provides


@Module
@InstallIn(SingletonComponent::class)
object AppComponents {
    @JvmStatic
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context?): AppDatabase {
        return AppDatabase.getAppDatabase(context!!)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNetworkManager(@ApplicationContext context: Context?): NetworkManager {
        return NetworkManager(context!!)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWorkExecutor(): WorkExecutor {
        return WorkExecutor()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @JvmStatic
    @Singleton
    @Provides
    fun provideBillingManager(
        @ApplicationContext context: Context?,
        appDatabase: AppDatabase?,
        networkManager: NetworkManager?,
        workExecutor: WorkExecutor?
    ): BillingManager {
        return BillingManager(context!!, appDatabase!!, networkManager!!, workExecutor!!)
    }
}