package com.example.talathiattendance.data.models


data class AttendanceModel(
    val uid:String? = "",
    val name:String? = "",
    val attendanceTime:String? = "",
    val imageUrl:String? = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "attendanceTime" to attendanceTime,
            "imageUrl" to imageUrl
        )
    }
}
