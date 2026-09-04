package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return try {
            UserRole.valueOf(value)
        } catch (e: Exception) {
            UserRole.ADMIN
        }
    }
}
