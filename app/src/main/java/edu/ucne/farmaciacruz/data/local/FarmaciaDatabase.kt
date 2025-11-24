package edu.ucne.farmaciacruz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.local.dao.PaymentOrderDao
import edu.ucne.farmaciacruz.data.local.entity.CarritoEntity
import edu.ucne.farmaciacruz.data.local.entity.PaymentOrderEntity

@Database(
    entities = [
        CarritoEntity::class,
        PaymentOrderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FarmaciaDatabase : RoomDatabase() {
    abstract fun carritoDao(): CarritoDao
    abstract fun paymentOrderDao(): PaymentOrderDao
}
