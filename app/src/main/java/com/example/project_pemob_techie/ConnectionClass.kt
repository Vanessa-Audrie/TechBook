package com.example.project_pemob_techie

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class ConnectionClass {

    private val db: String = "techbook-techie"
    private val ip: String = "10.0.2.2"
    private val port: String = "3306"
    private val username: String = "root"
    private val password: String = "123456"

    fun conn(): Connection? {
        var connection: Connection? = null
        val url = "jdbc:mysql://$ip:$port/$db"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return connection
    }
}
