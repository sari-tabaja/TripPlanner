package models

data class PlanItem(
    val id: String,
    val type: String,  // "flight", "lodging", "restaurant", "attraction"
    val name: String,
    val date: String,  // Ensure consistent date format (e.g., "yyyy-MM-dd")
    val time: String,  // Ensure consistent time format (e.g., "HH:mm")
    val isCheckIn: Boolean = true  // Differentiates check-in (true) and check-out (false) for lodgings
)


