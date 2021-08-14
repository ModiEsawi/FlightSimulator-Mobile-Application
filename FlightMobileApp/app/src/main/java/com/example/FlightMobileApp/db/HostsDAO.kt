package com.example.FlightMobileApp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data Access Object , manage hosts table
 */
@Dao
interface HostsDAO {

    /** insert host to db*/
    @Insert
    fun insertHost(host : HostsEntity)

    /** return all hosts in hosts table*/
    @Query("select * from HostsEntity")
    fun getHosts() : List<HostsEntity>

    /** delete host given it's name*/
    @Query ("Delete from HostsEntity Where host = :hostName ")
    fun deleteHost(hostName:String)

    /** increment latency for each host in hosts table**/
    @Query("Update HostsEntity SET latency = latency + 1")
    fun updateLatencies()

    /** sort hosts in hosts table by latency */
    @Query("select * from HostsEntity Order by latency")
    fun sortHosts() : List<HostsEntity>
}