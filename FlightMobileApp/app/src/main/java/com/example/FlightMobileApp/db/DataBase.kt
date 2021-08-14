package com.example.FlightMobileApp.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * database handling the hosts connection string
 */
@Database(entities = [(HostsEntity::class)],version = 2)
abstract class DataBase : RoomDatabase (){
    abstract fun hostDAO() : HostsDAO
}