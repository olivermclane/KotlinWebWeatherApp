/**
 * The main application class for the Weather Application.
 * Contains the main method to run the Spring Boot application.
 *
 * @author Oliver McLane
 */
package weather

import weather.services.GPTService
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import weather.models.DAO.WeatherDataDao
import weather.services.WeatherService
import java.io.IOException
import javax.servlet.http.HttpServletResponse

@SpringBootApplication
open class WeatherApplication{
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<WeatherApplication>(*args)
        }
    }
}

/**
 * Controller class responsible for handling HTTP requests related to weather information.
 * Integrates with weather services and AI services.
 */
@Controller
class WeatherController( @Value("\${openweathermap.api.key}") private val openweathermapApiKey: String,
                         @Value("\${openai.api.key}") private val openaiApiKey: String) {


    private val sessionFactory: SessionFactory = HibernateUtil.getSessionFactoryInstance()
    private val weatherDataDao: WeatherDataDao = WeatherDataDao(sessionFactory)
    private val weatherService: WeatherService = WeatherService(weatherDataDao,openweathermapApiKey)
    private val openaiService: GPTService = GPTService(openaiApiKey)

    // Index Routing
    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("title", "Home")
        return "index"
    }



    // Weather Routing
    @PostMapping("/weather")
    fun weatherpost(@RequestParam("city") city: String,
                    @RequestParam("state") state: String,
                    @RequestParam("country") country: String,
                    model: Model): String {

        // Replace spaces in the  with %20
        fun encodeSpaces(input: String): String {
            return input.replace(" ", "%20")
        }

        // Function to validate location using OpenStreetMap API
        fun validateLocation(city: String, country: String, state: String?): Boolean {
            val client = OkHttpClient()
            val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .addPathSegment("search")
                .addQueryParameter("format", "json")

            // Add country parameter
            urlBuilder.addQueryParameter("country", encodeSpaces(country))

            // Add state parameter if provided
            if (state != null && state.isNotEmpty()) {
                urlBuilder.addQueryParameter("state", encodeSpaces(state))
            }

            // Add city parameter if provided
            if (city.isNotEmpty()) {
                urlBuilder.addQueryParameter("city", encodeSpaces(city))
            }

            val url = urlBuilder.build()

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                val json = response.body!!.string()
                return json.contains("\"place_id\"") // Check if the JSON response contains a "place_id" to determine if the location is valid
            }
        }

        // Checks if the location provided in parameters is valid
        if(validateLocation(city, country, state)){
            // Call the weather service method to retrieve weather data
            weatherService.getWeatherData(city, state, country)
            val weatherData = weatherDataDao.getByCity(city)

            if (weatherData != null) {
                // Get AI response for the weather data
                val aiData = weatherData?.let { openaiService.getChatGPTResponse(it) }
                // Add the AI response to the model
                model.addAttribute("aiData", aiData)
            }

            // Add the weather data to the model
            model.addAttribute("weatherData", weatherData)
        }

        // Return the weather view
        return "index"
    }

    // Catch-all mapping other than /weather or /
    @RequestMapping("/**")
    fun redirectToIndex(response: HttpServletResponse) {
        response.sendRedirect("/")
    }
}
