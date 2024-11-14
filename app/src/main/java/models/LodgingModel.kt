package models

data class LodgingModel(
    var id: String = "",
    val lodgingName: String = "",
    val checkInDate: String = "",
    val checkInTime: String = "",
    val checkOutDate: String = "",
    val checkOutTime: String = "",
    val address: String = ""
)