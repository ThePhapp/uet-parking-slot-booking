package com.uet.parking.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    val userId: Int? = null, // userId=Column{notNull=false, primaryKeyPosition=1, defaultValue='undefined'}

    @ColumnInfo(name = "name", defaultValue = "NULL")
    val name: String? = null, // name=Column{notNull=false, primaryKeyPosition=0, defaultValue='NULL'}

    @ColumnInfo(name = "email")
    val email: String, // email=Column{notNull=true, primaryKeyPosition=0, defaultValue='undefined'}

    @ColumnInfo(name = "password", defaultValue = "NULL")
    val password: String? = null, // password=Column{notNull=false, primaryKeyPosition=0, defaultValue='NULL'}

    @ColumnInfo(name = "debt", defaultValue = "'0.00'")
    val debt: Double? = 0.0, // debt=Column{type='DECIMAL(15, 2)', affinity='1', notNull=false, defaultValue=''0.00''}

    @ColumnInfo(name = "role", defaultValue = "'user'")
    val role: String = "user" // role=Column{notNull=true, primaryKeyPosition=0, defaultValue=''user''}
)
