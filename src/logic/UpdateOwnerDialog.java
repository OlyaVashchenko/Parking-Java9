package logic;
/*
 * Custom dialog class, called by clicking the "Редактировать" button on the parkedOwners panel
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

class UpdateOwnerDialog extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateOwnerDialog(SessionFactory factory, JFrame frame, String ownerName, String ownerTel,
			JList<String> registeredJList, List<Car>registeredList, JList<String> parkedJList, List<Car>parkedList, List<CarOwner>ownersList) {
		super(frame, "Редактировать информацию", true);
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().setBackground(Color.WHITE);
		setBounds(300,300,400,250);
		
		JPanel name = new JPanel();
		JPanel tel = new JPanel();
		JPanel buttons = new JPanel();
		add(name);
		add(tel);
		add(buttons);
		
		JLabel nameLabel = new JLabel("Владелец: ");
		JLabel telLabel = new JLabel("Телефон: ");
		JTextField nameText = new JTextField(ownerName, 20);
		JTextField telText = new JTextField(ownerTel, 10);
		JButton ok = new JButton("Сохранить");
		JButton cancel = new JButton("Отмена");
		name.add(nameLabel);
		name.add(nameText);
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
				String newOwnerName = nameText.getText();
					if(!Pattern.matches(UICreator.ownerNamePattern.pattern(), newOwnerName)) {
						ErrorDialog.showErrorDialog(frame, "Неправильная марка/модель");
					}else {validated = true;}

				String ownerTel = telText.getText();
				if(ownerTel.length() == 0) {
					ownerTel = null;
					validated = true;
				}else {
					if(!Pattern.matches(UICreator.ownerTelPattern.pattern(), ownerTel)) {
						ErrorDialog.showErrorDialog(frame, "Неправильный телефон");
						validated = false;
					}else {
						if(newOwnerName == null) {
							ErrorDialog.showErrorDialog(frame, "Введите имя владельца");
							validated = false;
						}else {
							validated = true;
						}
					}
				}
				if(validated) {
					DBManager.updateCarOwner(factory, frame, ownerName, newOwnerName, ownerTel);
					UICreator.updateOwnersList(factory, frame, ownersList);
					UICreator.updateCarsList(factory, frame, registeredJList, registeredList, "registered");
					UICreator.updateCarsList(factory,  frame, parkedJList, parkedList, "parked");
					setVisible(false);
				}
			}
		});
	}
}
