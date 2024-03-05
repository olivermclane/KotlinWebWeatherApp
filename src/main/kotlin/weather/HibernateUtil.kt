/**
 * Utility class for managing Hibernate SessionFactory instance.
 */
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

object HibernateUtil {
    // Lazily initializes the SessionFactory instance using Hibernate Configuration
    private val sessionFactory: SessionFactory by lazy {
        val configuration = Configuration().configure("/hibernate.cfg.xml")
        configuration.buildSessionFactory()
    }

    /**
     * Retrieves the singleton instance of the Hibernate SessionFactory.
     * @return The SessionFactory instance.
     */
    fun getSessionFactoryInstance(): SessionFactory {
        return sessionFactory
    }
}
