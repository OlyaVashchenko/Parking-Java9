package logic;
/*
 * Custom dialog class, called by clicking the "Редактировать" button on the parkedCars panel
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hibernate.SessionFactory;

import util.DBManager;
import util.ErrorDialog;

class UpdateCarDialog extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1757673178575878706L;

	public UpdateCarDialog(SessionFactory factory, JFrame frame, String carNumber, String carModel, String carOwner, String ownerTel,
			JList<String> registeredJList, List<Car>registeredList, JList<String> parkedJList, List<Car>parkedList, List<CarOwner>ownersList) {
		super(frame, "Редактировать информацию", true);
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().setBackground(Color.WHITE);
		setBounds(300,300,400,250);
		
		JPanel model = new JPanel();
		JPanel owner = new JPanel();
		JPanel tel = new JPanel();
		JPanel buttons = new JPanel();
		add(model);
		add(owner);
		add(tel);
		add(buttons);
		
		JLabel modelLabel = new JLabel("Марка/модель:");
		JLabel ownerLabel = new JLabel("Владелец:");
		JLabel telLabel = new JLabel("Телефон:");
		JTextField modelText = new JTextField(carModel, 15);
		JTextField ownerText = new JTextField(carOwner, 20);
		JTextField telText = new JTextField(ownerTel, 10);
		JButton ok = new JButton("Сохранить");
		JButton cancel = new JButton("Отмена");
		model.add(modelLabel);
		model.add(modelText);
		owner.add(ownerLabel);
		owner.add(ownerText);
		tel.add(telLabel);
		tel.add(telText);
		buttons.add(ok);
		buttons.add(cancel);
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("cancel");
				setVisible(false);
			}
		});
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean validated = false;
				
				String model = modelText.getText();
				if(model.length() == 0) {
					model = null;
					validated = true;
				}else {
					if(!Pattern.matches(UICreator.carModelPattern.pattern(), model)) {
						ErrorDialog.showErrorDialog(frame, "Неправильная марка/модель");
					}else {validated = true;}
				}
				
				String ownerName = ownerText.getText();
				if(ownerName.length() == 0) {
					ownerName = null;
					validated = true;
				}else {
					if(!Pattern.matches(UICreator.ownerNamePattern.pattern(), ownerName)) {
						ErrorDialog.showErrorDialog(frame, "Неправильное имя владельца");
						validated = false;
					}else {validated = true;}
				}
				
				String ownerTel = telText.getText();
				if(ownerTel.length() == 0) {
					ownerTel = null;
					validated = true;
				}else {
					if(!Pattern.matches(UICreator.ownerTelPattern.pattern(), ownerTel)) {
						ErrorDialog.showErrorDialog(frame, "Неправильный телефон");
						validated = false;
					}else {
						if(ownerName == null) {
							ErrorDialog.showErrorDialog(frame, "Введите имя владельца");
							validated = false;
						}else {
							validated = true;
						}
					}
				}
				if(validated) {
					DBManager.updateRegisteredCar(factory, frame, carNumber, model, ownerName, ownerTel);
					UICreator.updateCarsList(factory, frame, registeredJList, registeredList, "registered");
					UICreator.updateCarsList(factory,  frame, parkedJList, parkedList, "parked");
					UICreator.updateOwnersList(factory, frame, ownersList);
					setVisible(false);
				}
			}
		});
	}
}
