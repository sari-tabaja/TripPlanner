package models

data class FlightModel(
    val flightDate: String = "",
    val departureTime: String = "",
    val departureAirport: String = "",
    val arrivalTime: String = "",
    val arrivalAirport: String = "",
    val airline: String = "",
    val flightNumber: String = "",
    val userId: String = "",
    val tripId: String = "",
    val imageUrl: String? = null
) {
    var flightId: String = ""
}

