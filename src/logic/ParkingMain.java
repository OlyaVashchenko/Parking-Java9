package logic;
import util.FactoryUtil;
import org.hibernate.SessionFactory;

public class ParkingMain{
	public static void main(String args[]){
		try{
			//System.out.println("before try in main");
			FactoryUtil.createFactory();
			SessionFactory myFactory = FactoryUtil.getFactory();
			UICreator myWindow = new UICreator();
			myWindow.createMainWindow(myFactory);
		}catch(Exception e) {
			//System.out.println("exception in main factory creation");
		}
	}
}