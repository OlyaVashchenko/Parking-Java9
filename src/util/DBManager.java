package util;
/*
 * The main class to work with database: retrieve, update, save, delete info
 * The methods return either int (index of element) or boolean (so that the developer can
 * include some checks in the main class based on the success of the operation)
 * Particularly this app version doesn't have such checks! 
 */
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.PersistenceException;
import javax.swing.JFrame;

import logic.Car;
import logic.CarOwner;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import util.ErrorDialog;

public class DBManager {
	private static Session session;
	private static boolean exist, success;
	private static Integer unregisteredCarId;
	
	public static long getNumberOfCars(SessionFactory sessionFactory, JFrame frame, String parameter, boolean value) {
		long count = 0;
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
			Root<Car> root = criteria.from(Car.class);
			criteria.select(builder.count(root)).where(builder.equal(root.get(parameter), value));
			count = session.createQuery(criteria).getSingleResult();
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблема с базой данных");
			count = -1;
		}finally {
			session.close();
		}
		return count;
	}
	
	public static boolean addRegisteredCar(SessionFactory sessionFactory, JFrame frame, String carNumber, 
			String carModel, String ownerName, String ownerTel, boolean parked, boolean ownerExists) {
		if(getNumberOfCars(sessionFactory, frame, "registered", true) == 15) {
			ErrorDialog.showErrorDialog(frame, "Достигнут лимит зарегестрированных автомобилей");
			return false;
		}else if(getNumberOfCars(sessionFactory, frame, "parked", true) == 20) {
			ErrorDialog.showErrorDialog(frame, "Достигнут лимит припаркованных автомобилей");
			return false;
		}else {
			if(carExistInTable(sessionFactory, frame, carNumber)) {
				ErrorDialog.showErrorDialog(frame, "Такой номер уже существует в базе");
				return false;
			}else {
				int carId = getNextId(sessionFactory, frame, "Car");
				int ownerId = getNextId(sessionFactory, frame, "CarOwner");
				LocalDateTime carParkedTime = LocalDateTime.now();
				session = sessionFactory.openSession();
				session.beginTransaction();
				try {
					Car newCar = new Car();
					newCar.setCarId(carId, frame);
					newCar.setCarNumber(carNumber, frame);
					newCar.setCarModel(carModel);
					newCar.setRegistered(true);
					newCar.setParked(parked);
					newCar.setCarParkedTime(carParkedTime, frame);
					if(ownerName == null) {
						//if ownerName is null, set carOwner field to null and save the object
						newCar.setCarOwner(null);
					}else{
						if(ownerExists) {
							CarOwner owner = session.createQuery("from CarOwner where ownerName = :on", CarOwner.class)
									.setParameter("on", ownerName).getSingleResult();
							newCar.setCarOwner(owner);
							owner.getCars().add(newCar);
						}else {
							//else create new carOwner object and save it and set carOwner field for car object and save car object
							CarOwner owner = new CarOwner();
							owner.setOwnerId(ownerId, frame);
							owner.setOwnerName(ownerName, frame);
							owner.setOwnerTel(ownerTel);
							newCar.setCarOwner(owner);
							session.save(owner);
						}
					}
					session.save(newCar);
					session.getTransaction().commit();
					success = true;
				}catch(Exception e) {
					if(session.getTransaction() != null) {
						session.getTransaction().rollback();
					}
					ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
					//System.out.println(e);
					success = false;
				}finally {
					session.close();	
				}
				return success;
			}
		}
	}
	
	public static boolean updateRegisteredCarParkedStatus(SessionFactory sessionFactory, JFrame frame, boolean parked, String carNumber) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			//select the car from db
			Car newCar = session.createQuery("from Car where carNumber = :c", Car.class)
					.setParameter("c", carNumber).getResultList().get(0); 
			if(parked == true) {
				newCar.setCarParkedTime(LocalDateTime.now(), frame);
			}
			newCar.setParked(parked);
			//update the column parked	
			session.saveOrUpdate(newCar);
			session.getTransaction().commit();
			success = true;
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
				ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
				//System.out.println(e);
				success = false;
			}
		}finally{
			session.close();
		}
		return success;
	}
	
	
	public static boolean deleteRegisteredCar(SessionFactory sessionFactory, JFrame frame, String carNumber) {
		List<Car> carList = new ArrayList<>();
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			Car car = session.createQuery("from Car where carNumber = :c", Car.class)
					.setParameter("c", carNumber).getSingleResult();
			if(car.getCarOwner() != null) {  
				//if carOwner exists, check how many car he has. If only one - delete car and carOwner, else delete only car
				CarOwner owner = (CarOwner) session.createQuery("from CarOwner where ownerName = :o")
						.setParameter("o", car.getCarOwner().getOwnerName()).getSingleResult();
				carList = owner.getCars();
				if(carList.size() > 1) {
					owner.getCars().remove(car);
					session.delete(car);
				}else {
					session.delete(owner);
				}
			}else {
				session.delete(car);
			}
			session.getTransaction().commit();
			success = true;
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
				ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
				//System.out.println(e);
				success = false;
			}
		}finally {
			session.close();
		}
		return success;
	}
	
	
	public static boolean deleteCarOwner(SessionFactory sessionFactory, JFrame frame, String ownerName) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			CarOwner owner = session.createQuery("from CarOwner where ownerName = :o", CarOwner.class)
					.setParameter("o", ownerName).getResultList().get(0);
			session.delete(owner);
			session.getTransaction().commit();
			success = true;
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
				ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
				//System.out.println(e);
				success = false;
			}
		}finally {
			session.close();
		}
		return success;
	}
	
	public static boolean updateRegisteredCar(SessionFactory sessionFactory, JFrame frame, String carNumber, String carModel,
			String ownerName, String ownerTel) {
		session = sessionFactory.openSession();
		session.getTransaction().begin();
		try {
			Car newCar = session.createQuery("from Car where carNumber = :n", Car.class)
					.setParameter("n", carNumber).getSingleResult();
			newCar.setCarModel(carModel);
			if(ownerName != null) {
				if(newCar.getCarOwner() != null) {
					CarOwner newOwner = session.createQuery("from CarOwner where ownerId = :id", CarOwner.class)
							.setParameter("id", newCar.getCarOwner().getOwnerId()).getSingleResult();
					newOwner.setOwnerName(ownerName, frame);
					newOwner.setOwnerTel(ownerTel);
					session.saveOrUpdate(newOwner);
				}else {
					CarOwner newOwner = new CarOwner();
					newOwner.setOwnerName(ownerName, frame);
					newOwner.setOwnerTel(ownerTel);
					session.save(newOwner);
					newCar.setCarOwner(newOwner);
				}
			}
			session.saveOrUpdate(newCar);
			session.getTransaction().commit();
			success = true;
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
			success = false;
		}finally {
			session.close();
		}
		return success;
	}
	
	public static boolean updateCarOwner(SessionFactory sessionFactory, JFrame frame, String oldName, String newName, String ownerTel) {
		session = sessionFactory.openSession();
		session.getTransaction().begin();
		try {
			CarOwner newOwner = session.createQuery("from CarOwner where ownerName = :name", CarOwner.class)
					.setParameter("name", oldName).getSingleResult();
			newOwner.setOwnerName(newName, frame);
			newOwner.setOwnerTel(ownerTel);
			session.saveOrUpdate(newOwner);
			session.getTransaction().commit();
			success = true;
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
			success = false;			
		}finally {
			session.close();
		}
		return success;
	}
	
	public static boolean addUnregisteredCar(SessionFactory sessionFactory, JFrame frame, String carNumber) {
		if(carExistInTable(sessionFactory, frame, carNumber)) {
			ErrorDialog.showErrorDialog(frame, "Такой номер уже существует в базе");
			success = false;
		}else {
			int carId = getNextId(sessionFactory, frame, "Car");
			session = sessionFactory.openSession();
			session.beginTransaction();
			try{
				
				Car newCar = new Car();
				newCar.setCarId(carId, frame);
				newCar.setCarModel(null);
				newCar.setCarNumber(carNumber, frame);
				newCar.setRegistered(false);
				newCar.setParked(true);
				newCar.setCarOwner(null);
				newCar.setCarParkedTime(LocalDateTime.now(), frame);
				session.save(newCar);
				session.getTransaction().commit();
				success = true;
			}catch(Exception e) {
				if(session.getTransaction() != null) {
					session.getTransaction().rollback();
				}
				ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
				//System.out.println(e);
				success = false;
			}finally {
				session.close();	
			}
		}
		return success;
	}
	
	public static boolean carExistInTable(SessionFactory sessionFactory, JFrame frame, String carNumber) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Integer> criteria = builder.createQuery(Integer.class);
			Root<Car> root = criteria.from(Car.class);
			criteria.select(root.get("carId"));
			criteria.where(builder.equal(root.get("carNumber"), carNumber));
			Query<Integer> query = session.createQuery(criteria);
			if(query.getResultList().isEmpty()) {	
				exist = false;
			}else {
				exist = true;
			}
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
		}
		finally {
			session.close();
		}
		return exist;
	}
	
	public static boolean ownerExistInTable(SessionFactory sessionFactory, JFrame frame, String ownerName) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<String> criteria = builder.createQuery(String.class);
			Root<CarOwner> root = criteria.from(CarOwner.class);
			criteria.select(root.get("ownerName"));
			criteria.where(builder.equal(root.get("ownerName"), ownerName));
			Query<String> query = session.createQuery(criteria);
			if(query.getResultList().isEmpty()) {	
				exist = false;
			}else {
				exist = true;
			}
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
		}
		finally {
			session.close();
		}
		return exist;
	}
	
	public static Integer unregisteredCarId(SessionFactory sessionFactory, JFrame frame, String carNumber) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Integer> criteria = builder.createQuery(Integer.class);
			Root<Car> root = criteria.from(Car.class);
			criteria.select(root.get("carId"));
			criteria.where(builder.equal(root.get("carNumber"), carNumber));
			Query<Integer> query = session.createQuery(criteria);
			if(!query.getResultList().isEmpty()) {	
				try {
					CriteriaQuery<Car> criteria2 = builder.createQuery(Car.class);
					Root<Car> root2 = criteria2.from(Car.class);
					criteria2.select(root2);
					criteria2.where(builder.equal(root2.get("carNumber"), carNumber));
					Car myCar = session.createQuery(criteria2).getSingleResult();
					if(myCar.getRegistered() == false) {unregisteredCarId = myCar.getCarId();
					} else {unregisteredCarId = 0;}
					session.getTransaction().commit();
				}catch(Exception e) {
					if(session.getTransaction() != null) {
						session.getTransaction().rollback();
					}
					ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
					//System.out.println(e);
					unregisteredCarId = -1;
				}
			}else {unregisteredCarId = 0;}	
		}catch(HibernateException e){
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
			unregisteredCarId = -1;
		}
		catch(PersistenceException e){
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
			unregisteredCarId = -1;
		}
		catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
			unregisteredCarId = -1;
		}
		finally {
			//System.out.println("criteria session closed");
			session.close();
		}
		return unregisteredCarId;
	}
	
	
	public static boolean deleteUnregisteredCar(SessionFactory sessionFactory, JFrame frame, Integer carId) {
		session = sessionFactory.openSession();
		session.beginTransaction();
		try {
			Car myCar = session.get(Car.class, carId);
			session.delete(myCar);
			success = true;
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
			success = false;
		} finally {
			session.close();
		}
		return success;
	}
	
	public static List<Car> selectAllParkedCars(SessionFactory sessionFactory, JFrame frame){
		List<Car> carsList = new ArrayList<>();
		session = sessionFactory.openSession();
		session.beginTransaction();
		//System.out.println("session opened");
		try{
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Car> criteria = builder.createQuery(Car.class);
			Root<Car> root = criteria.from(Car.class);
			criteria.select(root);
			criteria.where(builder.equal(root.get("parked"), true));
			criteria.orderBy(builder.desc(root.get("carParkedTime")));
			carsList = session.createQuery(criteria).getResultList();
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
		}finally {
			//System.out.println("session closed");
			session.close();	
		}
		return carsList;
	}
	
	public static List<Car> selectAllRegisteredCars(SessionFactory sessionFactory, JFrame frame){
		List<Car> carsList = new ArrayList<>();
		session = sessionFactory.openSession();
		session.beginTransaction();
		//System.out.println("session opened");
		try{
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Car> criteria = builder.createQuery(Car.class);
			Root<Car> root = criteria.from(Car.class);
			criteria.select(root);
			criteria.where(builder.equal(root.get("registered"), true));
			criteria.orderBy(builder.desc(root.get("carParkedTime")));
			carsList = session.createQuery(criteria).getResultList(); 	
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
		}finally {
			//System.out.println("session closed");
			session.close();	
		}
		return carsList;
	}
	
	public static List<CarOwner> selectOwners(SessionFactory sessionFactory, JFrame frame){
		List<CarOwner> owners = new ArrayList<>();
		session = sessionFactory.openSession();
		session.beginTransaction();
		try{
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<CarOwner> criteria = builder.createQuery(CarOwner.class);
			Root<CarOwner> root = criteria.from(CarOwner.class);
			criteria.select(root);
			criteria.orderBy(builder.asc(root.get("ownerName")));
			owners = session.createQuery(criteria).getResultList(); 	
			session.getTransaction().commit();
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
		}finally {
			//System.out.println("session closed");
			session.close();	
		}
		return owners;
	}
	
	public static int getNextId(SessionFactory sessionFactory, JFrame frame, String cls) {
		List<Integer> ids = new ArrayList<>();
		int nextId = 0;
		int startId = 1;
		session = sessionFactory.openSession();
		session.getTransaction().begin();
		try {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Integer> criteria = builder.createQuery(Integer.class);
			switch(cls) {
			case "Car":
				Root<Car> rootCar = criteria.from(Car.class);
				criteria.select(rootCar.get("carId")).orderBy(builder.asc(rootCar.get("carId")));
				ids = session.createQuery(criteria).getResultList(); 	
				break;
			case "CarOwner":
				Root<CarOwner> rootOwner = criteria.from(CarOwner.class);
				criteria.select(rootOwner.get("ownerId")).orderBy(builder.asc(rootOwner.get("ownerId")));
				ids = session.createQuery(criteria).getResultList(); 	
				break;
			}
			if(ids.size() < 1) {
				nextId =1;
			}else {
				for(int i=0; i<ids.size(); i++) {
					if(ids.get(i) != (i+startId)) {
						nextId = i+startId;
						break;
					}else {
						nextId = i+startId+1;
					}
				}
			}
		}catch(Exception e) {
			if(session.getTransaction() != null) {
				session.getTransaction().rollback();
			}
			ErrorDialog.showErrorDialog(frame, "Проблемы с базой данных");
			//System.out.println(e);
		}finally {
			//System.out.println("session closed");
			session.close();	
		}
		return (int)nextId;
	}	
}