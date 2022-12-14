package model

data class Weather(
    val city: City = getDefaultCity(),
    val temperature: Int = 0,
    val feelsLike: Int = -2
)

fun getDefaultCity() = City("Ростов-на-Дону", 47.2357137, 39.701505)