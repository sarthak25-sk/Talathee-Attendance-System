package com.example.talathiattendance.data.models


data class UserModel(
    val uid:String? = "",
    val name:String? = "",
    val email:String? = "",
    var isAdmin:Boolean = false,
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "isAdmin" to isAdmin
        )
    }
}
