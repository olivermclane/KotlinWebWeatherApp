/**
 * Service class for fetching weather data from the OpenWeatherMap API and saving it into the database.
 * @author Oliver McLane
 *
 * @property weatherDataDao The Data Access Object (DAO) for WeatherData entities.
 * @property openWeatherMapApiKey The API key for accessing the OpenWeatherMap API.
 */
package weather.services

import okhttp3.OkHttpClient
import okhttp3.HttpUrl
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonObject
import weather.models.WeatherData
import weather.models.DAO.WeatherDataDao
import java.io.IOException

class WeatherService(
    private val weatherDataDao: WeatherDataDao,
    private val openWeatherMapApiKey: String
) {
    private val gson = Gson()
    private val client = OkHttpClient()

    /**
     * Retrieves weather data from the OpenWeatherMap API based on the provided city, state, and country.
     * Saves the retrieved weather data into the database.
     * @param city The city for which weather data is requested.
     * @param state The state or region for which weather data is requested.
     * @param country The country for which weather data is requested.
     */
    fun getWeatherData(city: String, state: String, country: String) {
        val url = buildUrl(city, state, country)
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.code == 200) {
                parseAndSaveWeatherData(responseBody)
            }
        } catch (e: IOException) {
            // Log or handle the exception accordingly
        }
    }

    /**
     * Builds the URL for the OpenWeatherMap API request.
     * @param city The city for which weather data is requested.
     * @param state The state or region for which weather data is requested.
     * @param country The country for which weather data is requested.
     * @return The built URL for the API request.
     */
    private fun buildUrl(city: String, state: String, country: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.openweathermap.org")
            .addPathSegments("data/2.5/forecast")
            .addQueryParameter("q", "$city,$state,$country")
            .addQueryParameter("appid", openWeatherMapApiKey)
            .addQueryParameter("units", "imperial")
            .build()
    }

    /**
     * Parses the weather data from the OpenWeatherMap API response and saves it into the database.
     * @param responseBody The response body received from the OpenWeatherMap API.
     */
    private fun parseAndSaveWeatherData(responseBody: String?) {
        try {
            val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)

            // Parse city information
            val cityJson = jsonObject.getAsJsonObject("city")
            val cityName = cityJson.getAsJsonPrimitive("name").asString
            val countryCode = cityJson.getAsJsonPrimitive("country").asString

            val weatherList = jsonObject.getAsJsonArray("list")

            for (weatherElement in weatherList) {
                val weatherElementObject = weatherElement.asJsonObject

                val main = weatherElementObject.getAsJsonObject("main")
                val weatherArray = weatherElementObject.getAsJsonArray("weather")
                val weather = weatherArray[0].asJsonObject
                val wind = weatherElementObject.getAsJsonObject("wind")
                val dateCollected = weatherElementObject.getAsJsonPrimitive("dt_txt").asString

                val weatherData = WeatherData(
                    temperature = main.getAsJsonPrimitive("temp").asDouble,
                    highTemp = main.getAsJsonPrimitive("temp_max").asDouble,
                    lowTemp = main.getAsJsonPrimitive("temp_min").asDouble,
                    feltTemp = main.getAsJsonPrimitive("feels_like").asDouble,
                    humidity = main.getAsJsonPrimitive("humidity").asInt,
                    weatherDescription = weather.getAsJsonPrimitive("description").asString,
                    windSpeed = wind.getAsJsonPrimitive("speed").asDouble,
                    city = cityName,
                    countryCode = countryCode,
                    weatherCode = weather.getAsJsonPrimitive("icon").asString,
                    date = dateCollected,
                )

                weatherDataDao.save(weatherData)
            }
        } catch (e: Exception) {
        }
    }
}
