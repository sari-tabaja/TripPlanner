package models

data class TripModel(
    var tripId: String = "",
    val tripName: String = "",
    val startDate: String = "",
    val startTime: String = "",
    val endDate: String = "",
    val details: String = "",
    val sharedWith: MutableList<String> = mutableListOf() // List of user IDs for sharing
)





