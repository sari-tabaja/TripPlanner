package models

data class RestaurantModel(
    val restaurantName: String = "",
    val date: String = "",
    val time: String = "",
    val address: String = "",
    val phone: String = "",
    val website: String = "",
    val email: String = "",
    val confirmation: String = "",
    val userId: String = "",
    val tripId: String = "",
    var id: String = ""  // Firestore document ID
)
