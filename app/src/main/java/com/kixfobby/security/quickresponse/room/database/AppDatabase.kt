package com.kixfobby.security.quickresponse.room.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kixfobby.security.quickresponse.room.dao.BillingDao
import com.kixfobby.security.quickresponse.room.dao.ContactDao
import com.kixfobby.security.quickresponse.room.entity.BillingPurchaseDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases
import com.kixfobby.security.quickresponse.room.entity.ContactEntity


/**
 * Database Class, Creates Database, Database Instance and destroys Database instance.

 * @see [Room
 * Persistence Library Guide.](https://developer.android.com/topic/libraries/architecture/room.html)
 *
 * @see [Room
 * Persistence Library, A Reference Guide.](https://developer.android.com/reference/android/arch/persistence/room/package-summary.html)
 *
 * @since 1.0
 */
@Database(entities = [BillingSkuDetails::class, BillingPurchaseDetails::class, ContactEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    fun getIsThisSkuPurchased(skuID: String): LiveData<Boolean> {
        return Transformations.map(
            billingDao.getIsThisSkuPurchased(skuID)) { input: Int? -> input != null && input != 0 }
    }

    fun getSkuDetails(skuID: String): LiveData<BillingSkuDetails?> {
        return billingDao.getSkuDetails(skuID)
    }

    val skuRelatedPurchases: LiveData<List<BillingSkuRelatedPurchases?>?>
        get() = billingDao.skuRelatedPurchases

    fun insertPurchaseDetails(
        billingPurchaseDetailsList: List<BillingPurchaseDetails?>) {
        billingDao.insertPurchaseDetails(billingPurchaseDetailsList)
    }

    fun insertSkuDetails(billingSkuDetailsList: List<BillingSkuDetails?>) {
        billingDao.insertSkuDetails(billingSkuDetailsList)
    }

    /**
     * Gives BillingDao Database Operations.
     *
     * @return BillingDao abstract implementation.
     */
    abstract val billingDao: BillingDao
    abstract fun contactDao(): ContactDao

    companion object {
        private const val DATABASE_NAME = "MonetizeAppDB"

        /*private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("CREATE TABLE IF NOT EXISTS contact_table ()")
            }
        }*/

        @Volatile
        private var APP_DATABASE_INSTANCE: AppDatabase? = null

        /**
         * Creates Room Database Instance if was not already initiated.
         *
         * @param context Activity or Application Context.
         * @return [.APP_DATABASE_INSTANCE]
         */
        @Synchronized
        fun getAppDatabase(context: Context): AppDatabase {
            if (APP_DATABASE_INSTANCE == null) {
                APP_DATABASE_INSTANCE =
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration().build()
            }
            return APP_DATABASE_INSTANCE!!
        }
    }

    /*companion object {

        @Volatile
        private var INSTANCE: QrDatabase? = null
        fun getDatabase(context: Context): QrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QrDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance

                instance
            }
        }

    }*/
}