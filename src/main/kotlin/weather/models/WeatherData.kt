package weather.models
import javax.persistence.*
/**
 * Represents weather data to be stored in the database.
 *
 * @author Oliver McLane
 *
 * @param id The unique identifier for the weather data entry.
 * @param temperature The temperature recorded.
 * @param highTemp The highest temperature recorded.
 * @param lowTemp The lowest temperature recorded.
 * @param feltTemp The perceived temperature.
 * @param humidity The humidity level.
 * @param weatherDescription A description of the weather conditions.
 * @param windSpeed The wind speed.
 * @param city The city for which the weather data is recorded.
 * @param countryCode The country code of the location.
 * @param weatherCode The weather code for icon.
 * @param date The date when the weather data was collected.
 */
@Entity
@Table(name = "WEATHER_DATA")
data class WeatherData(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    var id: Long? = null,

    @Column(name = "TEMPERATURE")
    var temperature: Double = 0.0,

    @Column(name = "HIGH_TEMP")
    var highTemp: Double = 0.0,

    @Column(name = "LOW_TEMP")
    var lowTemp: Double = 0.0,

    @Column(name = "FELT_TEMP")
    var feltTemp: Double = 0.0,

    @Column(name = "HUMIDITY")
    var humidity: Int = 0,

    @Column(name = "WEATHER_DESC")
    var weatherDescription: String = "",

    @Column(name = "WIND_SPEED")
    var windSpeed: Double = 0.0,

    @Column(name = "CITY")
    var city: String = "",

    @Column(name = "COUNTRY_CODE")
    var countryCode: String = "",

    @Column(name = "WEATHER_CODE")
    var weatherCode: String = "",

    @Column(name = "DATE_COLLECTED")
    var date: String = "",
)
