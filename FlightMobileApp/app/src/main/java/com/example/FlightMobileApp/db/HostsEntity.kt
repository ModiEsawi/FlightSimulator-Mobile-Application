package com.example.FlightMobileApp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Hosts Table , two attributes : host and latency .
 */
@Entity
class HostsEntity {

    @PrimaryKey
    var host : String = ""
    var latency : Int = 0
}