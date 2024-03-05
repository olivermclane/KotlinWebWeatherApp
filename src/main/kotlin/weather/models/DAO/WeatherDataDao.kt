/**
 * This class provides data access operations for WeatherData objects using Hibernate.
 *
 * @author Oliver McLane
 */
package weather.models.DAO
import org.hibernate.SessionFactory
import weather.models.WeatherData

class WeatherDataDao(private val sessionFactory: SessionFactory) {

    /**
     * Saves WeatherData object to the database.
     * Checks for duplicate records based on city and date before saving.
     * @param weatherData the WeatherData object to be saved
     */
    fun save(weatherData: WeatherData) {

        val session = sessionFactory.openSession()
        session.beginTransaction()

        // Check if a similar record already exists based on city and date
        val existingData = getByCity(weatherData.city)
        val duplicate = existingData?.find { it.date == weatherData.date }

        // If no duplicate is found, save the WeatherData object
        if (duplicate == null) {
            session.save(weatherData)
        }

        // Commit the transaction
        session.transaction.commit()

        // Close the session
        session.close()
    }

    /**
     * Retrieves WeatherData objects by city from the database.
     * @param city the city name
     * @return a list of WeatherData objects matching the city, or null if none found
     */
    fun getByCity(city: String): List<WeatherData>? {
        val session = sessionFactory.openSession()

        // Constructing HQL (Hibernate Query Language) query to fetch WeatherData by city
        val query = session.createQuery("FROM WeatherData WHERE city = :city")
        query.setParameter("city", city)

        // Retrieving a list of WeatherData objects matching the city
        val weatherData = query.list() as List<WeatherData>

        session.close()

        // Returning the first element of the list, or null if the list is empty
        return weatherData as List<WeatherData>?
    }
}
