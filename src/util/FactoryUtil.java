package util;
import org.hibernate.SessionFactory;
//import org.hibernate.cfg.Configuration;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
/*
 * Class to manage the SessionFactory (that will further create one SessionFactory for app instance to give us sessions, and 
 * close this Factory)
 */
public class FactoryUtil {
	private static SessionFactory factory;
	
	public static void createFactory() throws Exception{
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
		System.out.println("before factory try");
		try {
			factory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
			System.out.println("factory opened");
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			StandardServiceRegistryBuilder.destroy( registry );
			System.out.println("exception in factory creation: "+e);
		}
	}
	
	public static SessionFactory getFactory() {
		System.out.println("get factory");
		return factory;
	}
	
	public static void closeFactory() throws Exception{
		if (factory != null ) {
			factory.close();
			System.out.println("Factory closed");
		}
	}
}