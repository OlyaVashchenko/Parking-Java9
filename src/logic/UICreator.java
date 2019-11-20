package logic;
/*
 * The main class: creates the app window, fills all the lists, responds to user's actions, ect.
 */
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;

import org.hibernate.SessionFactory;

import util.*;

//the main application class, contains all the UI elements and handlers
public class UICreator {

	private static Color frontColor = new Color(7,186,159);     //icon, borders, text color    
	private static Color backColor = new Color(230, 254, 251);  //background color
	private static Font myFont = new Font("Lucida", Font.PLAIN, 14);
	private final String IMAGEPATH = "images\\icon.png";
	private static String carNumber, carModel, carOwner, ownerTel;   //static strings for newCarPanel (register new car)
	public static final Pattern carNumberPattern = Pattern.compile("^[0-9]{4}[а-зій-уўф-шьыэюяА-ЗІЙ-УЎФ-ШЬЫЭЮЯ]{2}-{1}[1-7]{1}$");
	public static final Pattern carModelPattern = Pattern.compile("^[a-zA-Zа-яА-Я0-9\\s-/]{1,100}$");
	public static final Pattern ownerNamePattern = Pattern.compile("^[a-zA-Zа-яА-Я\\s]{1,200}$");
	public static final Pattern ownerTelPattern = Pattern.compile("^[+]+(375)+(29|33|44|25|17)+[0-9]{7}$");
	
	//updates either parkedCarsList or registeredCarsList, is called after any change made to these lists
	//parkedCarsList needs a specific blue-gray cellRenderer to differentiate registered and unregistered cars
	public static void updateCarsList(SessionFactory factory, JFrame frame, JList<String>jList, List<Car>list, String param) {
		list.clear();
		if(param == "parked") {
			list.addAll(DBManager.selectAllParkedCars(factory, frame));
			jList.setCellRenderer(new BlueGrayCellRenderer(list, frontColor));
		}else if(param == "registered") {
			list.addAll(DBManager.selectAllRegisteredCars(factory, frame));
		}	
	}
	//updates ownersList, is called after any change made to it
	public static void updateOwnersList(SessionFactory factory, JFrame frame, List<CarOwner>list) {
		list.clear();
		list.addAll(DBManager.selectOwners(factory, frame));
	}
	//creates a multiline label by formatting JTextArea
	private static JTextArea createMultilineLabel(Color background, Font font) {
		JTextArea label = new JTextArea();
		label.setBackground(null);
		label.setEditable(false);
		label.setBorder(null);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		label.setFocusable(false);
		label.setFont(font);
		label.setOpaque(true);
		label.setBackground(background);
		label.setPreferredSize(new Dimension(150,100)); //width-height integers
		return label;
	}
	//creates a button
	private static JButton createButton(Color background, String text, int width, int height) {
		JButton button = new JButton(text);
		button.setBackground(background);
		button.setFont(myFont);
		button.setMinimumSize(new Dimension(width, height));
		button.setPreferredSize(new Dimension(width, height));
		button.setMaximumSize(new Dimension(width*2, height*2));
		return button;
	}
	//creates a title label (bordered, centered)
	private static JLabel createTitleLabel(Color background, Color color, String text, int width, int height) {
		JLabel label = new JLabel(text);
		label.setOpaque(true);
		label.setBackground(background);
		label.setBorder(BorderFactory.createMatteBorder(2, 1, 0, 1, color));
		label.setFont(myFont);
		label.setMinimumSize(new Dimension(width, height));
		label.setPreferredSize(new Dimension(width, height));
		label.setMaximumSize(new Dimension(width*2, height*2));
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}
	
	public void createMainWindow(SessionFactory factory) {
		JFrame f = new JFrame();
		f.setTitle("Парковка");
		f.setBounds(300,50,700,500);
		Image icon = Toolkit.getDefaultToolkit().getImage(IMAGEPATH);
		f.setIconImage(icon);
		f.getContentPane().setBackground(Color.WHITE);
		
		Container pane = f.getContentPane();
		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
		
		///////TITLE PANEL (ONLY TITLES)\\\\\\\
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,20)); //alignment, horiz and vert gap
		titlePanel.setMinimumSize(new Dimension(700,70)); //top left = 20,20
		titlePanel.setPreferredSize(new Dimension(700,70));
		titlePanel.setBackground(Color.WHITE);
		titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JLabel parkedLabel = createTitleLabel(backColor, frontColor, "Парковка",180,50);
		JLabel registeredLabel = createTitleLabel(backColor, frontColor,"Зарегистрированные",180,50);
		JLabel newCarLabel = createTitleLabel(backColor, frontColor,"Новый автомобиль",180,50);

		titlePanel.add(parkedLabel);
		titlePanel.add(registeredLabel);
		titlePanel.add(newCarLabel);
		
		
		///////CONTENT PANEL(CONTAINS ALL 3 TABS)\\\\\\\\
		Container contentPanel = new Container();
		contentPanel.setMinimumSize(new Dimension(700,360)); //top left = 20, 120
		contentPanel.setPreferredSize(new Dimension(700,360));
		contentPanel.setLocation(0, 120);
		
		
		///////ПАРКОВКА tab\\\\\\\
		JPanel parkedPanel = new JPanel();
		parkedPanel.setLayout(new BoxLayout(parkedPanel, BoxLayout.X_AXIS));
		parkedPanel.setMinimumSize(new Dimension(700,360)); //top left = 20, 120
		parkedPanel.setPreferredSize(new Dimension(700,360));
		parkedPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, frontColor));
		parkedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		parkedPanel.setBackground(backColor);
		
		JPanel parkedListPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,10));
		parkedListPanel.setPreferredSize(new Dimension(250,355));
		parkedListPanel.setBackground(backColor);
		
		JPanel parkedInfoPanel = new JPanel();
		parkedInfoPanel.setPreferredSize(new Dimension(350,355));
		parkedInfoPanel.setLayout(new BoxLayout(parkedInfoPanel, BoxLayout.Y_AXIS));
		parkedInfoPanel.setBackground(backColor);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
		infoPanel.setBackground(backColor);
		JPanel inOutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
		inOutPanel.setBackground(backColor);
		JPanel togoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
		togoPanel.setBackground(backColor);

		
		//////ПАРКОВКА COMPONENTS\\\\\\\
		JTextArea parkedCarInfo = createMultilineLabel(frontColor, myFont);
		parkedCarInfo.setPreferredSize(new Dimension(300,120));
		
		JLabel toGoLabel = new JLabel(); //text to indicate whether the car is registered ("направо") or not ("налево")
		toGoLabel.setFont(myFont);
		
		List<Car> myParkedList = new ArrayList<>(DBManager.selectAllParkedCars(factory, f));
		DefaultListModel<String> parkedNumbersModel = new DefaultListModel<>();
		for(int i=0; i<myParkedList.size(); i++) {parkedNumbersModel.addElement(myParkedList.get(i).getCarNumber());}
		JList<String> parkedCarsList = new JList<>(parkedNumbersModel);
		parkedCarsList.setCellRenderer(new BlueGrayCellRenderer(myParkedList, frontColor));
		parkedCarsList.setSelectedIndex(0);
		JScrollPane parkedScroll = new JScrollPane(parkedCarsList);  //make list scrollable
		parkedScroll.setPreferredSize(new Dimension(200,320));

		JButton newCarInButton = createButton(Color.WHITE,"Заезд",100,50);
		JButton newCarOutButton = createButton(Color.WHITE,"Выезд",100,50);
		if(parkedNumbersModel.getSize() > 0) {
			newCarOutButton.setEnabled(true);
		}else {newCarOutButton.setEnabled(false);}
		
		infoPanel.add(parkedCarInfo);
		inOutPanel.add(newCarInButton);
		inOutPanel.add(newCarOutButton);
		togoPanel.add(toGoLabel);
		
		parkedListPanel.add(parkedScroll);
		parkedInfoPanel.add(infoPanel);
		parkedInfoPanel.add(inOutPanel);
		parkedInfoPanel.add(togoPanel);	
		
		parkedPanel.add(parkedListPanel);
		parkedPanel.add(parkedInfoPanel);
		
		//////ЗАРЕГИСТРИРОВАННЫЕ tab\\\\\\\
		JPanel registeredPanel = new JPanel();
		registeredPanel.setLayout(new BoxLayout(registeredPanel, BoxLayout.Y_AXIS));
		registeredPanel.setMinimumSize(new Dimension(700,360)); //top left = 20, 120
		registeredPanel.setPreferredSize(new Dimension(700,360));
		registeredPanel.setBackground(Color.WHITE);
		registeredPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JPanel registeredTitles = new JPanel();
		registeredTitles.setLocation(0, 120);
		registeredTitles.setPreferredSize(new Dimension(700, 55));
		registeredTitles.setBackground(Color.WHITE);
		
		JLabel registeredCarsLabel = createTitleLabel(backColor, frontColor,"Автомобили",100,50);		
		JLabel registeredOwnersLabel = createTitleLabel(backColor, frontColor,"Владельцы",100,50);
		
		registeredTitles.add(registeredCarsLabel);
		registeredTitles.add(registeredOwnersLabel);
		
		JPanel registeredCont = new JPanel();
		registeredCont.setLocation(0, 170);
		registeredCont.setPreferredSize(new Dimension(700,280));
		registeredCont.setBackground(backColor);
		registeredCont.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, frontColor));
		
		registeredPanel.add(registeredTitles);
		registeredPanel.add(registeredCont);
		
		JPanel registeredCarsPanel = new JPanel();
		registeredCarsPanel.setBackground(backColor);
		
		JPanel registeredOwnersPanel = new JPanel();
		registeredOwnersPanel.setBackground(backColor);
		
		CardLayout registeredCards = new CardLayout(0,0);
		registeredCards.addLayoutComponent(registeredCarsPanel, "cars");
		registeredCards.addLayoutComponent(registeredOwnersPanel, "owners");
		registeredCont.setLayout(registeredCards);
		registeredCards.show(registeredCont, "cars");

		registeredCont.add(registeredCarsPanel);
		registeredCont.add(registeredOwnersPanel);
		

		///////ЗАРЕГИСТРИРОВАННЫЕ COMPONENTS\\\\\\\\
		JPanel regCarListPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
		regCarListPanel.setPreferredSize(new Dimension(310,325));
		JPanel regCarInfoPanel = new JPanel();
		regCarInfoPanel.setLayout(new BoxLayout(regCarInfoPanel, BoxLayout.Y_AXIS));
		regCarInfoPanel.setPreferredSize(new Dimension(310,325));
		
		JPanel rCarInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
		JPanel regCarButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
		rCarInfoPanel.setBackground(backColor);
		regCarButtonsPanel.setBackground(backColor);
		regCarListPanel.setBackground(backColor);
		
		
		JTextArea registeredCarInfo = createMultilineLabel(frontColor, registeredLabel.getFont());
		registeredCarInfo.setPreferredSize(new Dimension(300,120));
		
		List<Car> myRegisteredList = new ArrayList<>(DBManager.selectAllRegisteredCars(factory, f));
		DefaultListModel<String> registeredNumbersModel = new DefaultListModel<>();
		for(int i=0; i<myRegisteredList.size(); i++) {registeredNumbersModel.addElement(myRegisteredList.get(i).getCarNumber());}
		JList<String> registeredCarsList = new JList<>(registeredNumbersModel);
		registeredCarsList.setForeground(frontColor);
		registeredCarsList.setSelectedIndex(0);
		JScrollPane carsScroll = new JScrollPane(registeredCarsList);  //make list scrollable
		carsScroll.setPreferredSize(new Dimension(200,220));

		JPanel regOwnerListPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
		regOwnerListPanel.setPreferredSize(new Dimension(310,325));
		JPanel regOwnerInfoPanel = new JPanel();
		regOwnerInfoPanel.setLayout(new BoxLayout(regOwnerInfoPanel, BoxLayout.Y_AXIS));
		regOwnerInfoPanel.setPreferredSize(new Dimension(310,325));
		
		JPanel rOwnerInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
		JPanel regOwnerButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
		rOwnerInfoPanel.setBackground(backColor);
		regOwnerButtonsPanel.setBackground(backColor);
		regOwnerListPanel.setBackground(backColor);
		
		List<CarOwner> myOwnersList = new ArrayList<>(DBManager.selectOwners(factory, f));
		DefaultListModel<String> ownerNamesModel = new DefaultListModel<>();
		for(int i=0; i<myOwnersList.size(); i++) {ownerNamesModel.addElement(myOwnersList.get(i).getOwnerName());}
		JList<String> ownersList = new JList<>(ownerNamesModel);
		ownersList.setForeground(frontColor);
		ownersList.setSelectedIndex(0);
		DefaultListModel<String> ownerCarsModel = new DefaultListModel<>();
		JScrollPane ownersScroll = new JScrollPane(ownersList);  //make list scrollable
		ownersScroll.setPreferredSize(new Dimension(200,100));
		
		JButton updateRegisteredCar = createButton(Color.WHITE,"Редактировать",150,50);
		JButton deleteRegisteredCar = createButton(Color.WHITE,"Удалить",150,50);
		JButton deleteOwner = createButton(Color.WHITE,"Удалить",150,50);
		JButton updateOwner = createButton(Color.WHITE,"Редактировать",150,50);
		
		regCarListPanel.add(carsScroll);
		rCarInfoPanel.add(registeredCarInfo);
		regCarButtonsPanel.add(updateRegisteredCar);
		regCarButtonsPanel.add(deleteRegisteredCar);
		regCarInfoPanel.add(rCarInfoPanel);
		regCarInfoPanel.add(regCarButtonsPanel);
		registeredCarsPanel.add(regCarListPanel);
		registeredCarsPanel.add(regCarInfoPanel);
		
		JTextArea title = new JTextArea();
		title.setBackground(null);
		title.setEditable(false);
		title.setLineWrap(true);
		title.setWrapStyleWord(true);
		title.setFocusable(false);
		title.setFont(registeredOwnersLabel.getFont());
		
		JList<String> ownerCarsList = new JList<>();
		JScrollPane ownerCarsScroll = new JScrollPane(ownerCarsList);
		ownerCarsScroll.setPreferredSize(new Dimension(300,120));
		ownerCarsScroll.setColumnHeaderView(title);
		
		regOwnerListPanel.add(ownersScroll);
		rOwnerInfoPanel.add(ownerCarsScroll);
		regOwnerButtonsPanel.add(updateOwner);
		regOwnerButtonsPanel.add(deleteOwner);
		regOwnerInfoPanel.add(rOwnerInfoPanel);
		regOwnerInfoPanel.add(regOwnerButtonsPanel);
		registeredOwnersPanel.add(regOwnerListPanel);
		registeredOwnersPanel.add(regOwnerInfoPanel);
		
		
		///////НОВЫЙ АВТОМОБИЛЬ tab\\\\\\\
		JPanel newCarPanel = new JPanel();
		newCarPanel.setBackground(Color.WHITE);
		newCarPanel.setLayout(new BoxLayout(newCarPanel, BoxLayout.Y_AXIS));
		newCarPanel.setMinimumSize(new Dimension(700,360)); //top left = 20, 120
		newCarPanel.setPreferredSize(new Dimension(700,360));
		newCarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		newCarPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, frontColor));

		JPanel numberPanel = new JPanel();
		numberPanel.setBackground(backColor);
		JPanel numberPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,15));
		numberPanel1.setPreferredSize(new Dimension(250, 50));
		numberPanel1.setBackground(backColor);
		JPanel numberPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT,5,15));
		numberPanel2.setPreferredSize(new Dimension(350, 50));
		numberPanel2.setBackground(backColor);
		
		JPanel modelPanel = new JPanel();
		modelPanel.setBackground(backColor);
		JPanel modelPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,15));
		modelPanel1.setPreferredSize(new Dimension(250, 50));
		modelPanel1.setBackground(backColor);
		JPanel modelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT,5,15));
		modelPanel2.setPreferredSize(new Dimension(350, 50));
		modelPanel2.setBackground(backColor);
		
		JPanel ownerPanel = new JPanel();
		ownerPanel.setBackground(backColor);
		JPanel ownerPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,15));
		ownerPanel1.setPreferredSize(new Dimension(250, 50));
		ownerPanel1.setBackground(backColor);
		JPanel ownerPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT,5,15));
		ownerPanel2.setPreferredSize(new Dimension(350, 50));
		ownerPanel2.setBackground(backColor);
		
		JPanel telPanel = new JPanel();
		telPanel.setBackground(backColor);
		JPanel telPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,15));
		telPanel1.setPreferredSize(new Dimension(250, 50));
		telPanel1.setBackground(backColor);
		JPanel telPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT,5,15));
		telPanel2.setPreferredSize(new Dimension(350, 50));
		telPanel2.setBackground(backColor);
		
		JPanel savePanel = new JPanel();
		savePanel.setBackground(backColor);
		JPanel savePanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,15));
		savePanel1.setPreferredSize(new Dimension(250, 70));
		savePanel1.setBackground(backColor);
		JPanel savePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,15));
		savePanel2.setPreferredSize(new Dimension(350, 70));
		savePanel2.setBackground(backColor);
		
		
		//////НОВЫЙ АВТОМОБИЛЬ COMPONENTS\\\\\\\
		JLabel carNumberLabel = new JLabel("Регистрационный номер:");
		JTextField carNumberText = new JTextField(10);
		JLabel carModelLabel = new JLabel("Марка/модель автомобиля:");
		JTextField carModelText = new JTextField(25);
		JLabel carOwnerLabel = new JLabel("Владелец:");
		JTextField carOwnerText = new JTextField(25);
		JLabel ownerTelLabel = new JLabel("Телефон:");
		JTextField ownerTelText = new JTextField(10);
		JButton saveNewCar = createButton(Color.WHITE,"Сохранить",150,50);
		carNumberLabel.setFont(myFont);
		carNumberText.setFont(myFont);
		carModelLabel.setFont(myFont);
		carModelText.setFont(myFont);
		carOwnerLabel.setFont(myFont);
		carOwnerText.setFont(myFont);
		ownerTelLabel.setFont(myFont);
		ownerTelText.setFont(myFont);
		saveNewCar.setFont(myFont);
		
		numberPanel1.add(carNumberLabel);
		numberPanel2.add(carNumberText);
		modelPanel1.add(carModelLabel);
		modelPanel2.add(carModelText);
		ownerPanel1.add(carOwnerLabel);
		ownerPanel2.add(carOwnerText);
		telPanel1.add(ownerTelLabel);
		telPanel2.add(ownerTelText);
		savePanel2.add(saveNewCar);
		
		numberPanel.add(numberPanel1);
		numberPanel.add(numberPanel2);
		newCarPanel.add(numberPanel);
		modelPanel.add(modelPanel1);
		modelPanel.add(modelPanel2);
		newCarPanel.add(modelPanel);
		ownerPanel.add(ownerPanel1);
		ownerPanel.add(ownerPanel2);
		newCarPanel.add(ownerPanel);
		telPanel.add(telPanel1);
		telPanel.add(telPanel2);
		newCarPanel.add(telPanel);
		savePanel.add(savePanel1);
		savePanel.add(savePanel2);
		newCarPanel.add(savePanel);
		
		//////MAIN LAYOUT - 3 TABS\\\\\\\
		CardLayout cards = new CardLayout(0,0);  
		cards.addLayoutComponent(parkedPanel,"parked");
		cards.addLayoutComponent(registeredPanel, "registered");
		cards.addLayoutComponent(newCarPanel, "newCar");
		contentPanel.setLayout(cards);
		
		contentPanel.add(parkedPanel);
		contentPanel.add(registeredPanel);
		contentPanel.add(newCarPanel);
		
		cards.show(contentPanel,"parked");
		pane.add(titlePanel);
		pane.add(contentPanel);
		//---------------------------------
		
		
		/////ADD LISTENERS TO ALL THE COMPONENTS\\\\\\
		newCarInButton.addActionListener((event)-> {
			parkedCarInfo.setText(null);
			String carNumber = JOptionPane.showInputDialog(f, "Введите номер автомобиля", "Новый заезд", JOptionPane.QUESTION_MESSAGE);
			if(carNumber == null) {}
			else if(carNumber.length() == 0 || !Pattern.matches(carNumberPattern.pattern(), carNumber)) {
				ErrorDialog.showErrorDialog(f, "Неправильный формат номера");
			}else{
				if(DBManager.carExistInTable(factory, f, carNumber)) {
					if(DBManager.getNumberOfCars(factory, f, "parked", true) == 20) {
						ErrorDialog.showErrorDialog(f, "Достигнут лимит припаркованных автомобилей");
					}else {
						if(DBManager.updateRegisteredCarParkedStatus(factory, f, true, carNumber)) {
							toGoLabel.setText("НАПРАВО!");
							toGoLabel.setForeground(frontColor);
						}
					}
				}else{
					if(DBManager.getNumberOfCars(factory, f, "parked", true) == 20) {
						ErrorDialog.showErrorDialog(f, "Достигнут лимит припаркованных автомобилей");
					}else if(DBManager.getNumberOfCars(factory, f, "registered", false) == 5) {	
						ErrorDialog.showErrorDialog(f, "Достигнут лимит незарегистрированных автомобилей");
					}else {
						if(DBManager.addUnregisteredCar(factory, f, carNumber)) {
							toGoLabel.setText("НАЛЕВО!");
							toGoLabel.setForeground(Color.DARK_GRAY);
						}
					}
				}

				updateCarsList(factory, f, parkedCarsList, myParkedList, "parked");
				parkedNumbersModel.insertElementAt(carNumber, 0); 
				parkedCarInfo.setText(null);
				if(parkedNumbersModel.getSize() > 0) {newCarOutButton.setEnabled(true);}
			}
		});

		
		parkedCarsList.addListSelectionListener((event)-> {
			int index = parkedCarsList.getSelectedIndex();
			String value = parkedCarsList.getSelectedValue();
			if(parkedNumbersModel.size() > 0 && parkedNumbersModel != null && index >= 0 && value != null) {
				if(!event.getValueIsAdjusting()) {
					Car selectedCar = myParkedList.get(index);
					String registered = selectedCar.getRegistered() ? "зарегистрирован" : "гость";
					String carModel = selectedCar.getCarModel()==null ? "не указана" : selectedCar.getCarModel();
					String ownerName = selectedCar.getCarOwner()==null ? "не указан" : selectedCar.getCarOwner().getOwnerName();
					String ownerTel;
					if(ownerName == "не указан") {
						ownerTel = "не указан";
					}else {
						ownerTel = selectedCar.getCarOwner().getOwnerTel()==null ? "не указан" : selectedCar.getCarOwner().getOwnerTel();
					}
					parkedCarInfo.setText("Марка/модель: "+carModel+"\nСтатус: "+registered+"\nВладелец: "+ownerName+"\nТелефон: "+ownerTel);
				}
			}
			if(index != -1) {
				toGoLabel.setForeground(Color.DARK_GRAY);
				toGoLabel.setText(null);
			}
		});
		

		newCarOutButton.addActionListener((event)-> {
			toGoLabel.setText(null);
			parkedCarInfo.setText(null);
			String carNumber = parkedCarsList.getSelectedValue();
				
			Integer carId = DBManager.unregisteredCarId(factory, f, carNumber);
			if(carId > 0) {
				DBManager.deleteUnregisteredCar(factory, f, carId);
			}else if(carId == 0){
				DBManager.updateRegisteredCarParkedStatus(factory, f, false, carNumber);
			}

			updateCarsList(factory, f, parkedCarsList, myParkedList, "parked");
			parkedNumbersModel.removeElement(carNumber);
			parkedCarInfo.setText(null);
		});
		
		saveNewCar.addActionListener((event)-> {
			boolean ownerExists = false;
			parkedCarInfo.setText(null);
			boolean validated = false;
			carNumber = carNumberText.getText();
			if(!Pattern.matches(carNumberPattern.pattern(), carNumber) || carNumber.isEmpty()) {
				ErrorDialog.showErrorDialog(f, "Введите правильный номер автомобиля");
			}else{
					
				carModel = carModelText.getText();
				if(!Pattern.matches(carModelPattern.pattern(), carModel)) {
					if(carModel.isEmpty()) {carModel = null; validated = true;} else {
						ErrorDialog.showErrorDialog(f, "Введите правильную модель/марку");
						validated = false;
					}	
				}else {validated = true;}
				
				carOwner = carOwnerText.getText();
				if(!Pattern.matches(ownerNamePattern.pattern(), carOwner)) {
					if(carOwner.isEmpty()) {
						carOwner = null; 
						validated = true;
						ownerTel = null;
					} else {
						ErrorDialog.showErrorDialog(f, "Введите правильное имя владельца");
						validated = false;
					}	
				}else {
					ownerExists = DBManager.ownerExistInTable(factory, f, carOwner);
					if(ownerExists) {
						Object[] options = {"Да", "Нет"};
						int choice = JOptionPane.showOptionDialog(f, "Такой владелец уже существует в базе.\n Зарегестрировать автомобиль на него?", 
									"Владелец уже существует", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if(choice == 0) {
							validated = true;
						}else {validated = false;}
					}else {
						validated = true;
						ownerTelText.setEnabled(true);
						ownerTel = ownerTelText.getText();
						if(!Pattern.matches(ownerTelPattern.pattern(), ownerTel)) {
							if(ownerTel.isEmpty()) {ownerTel = null; validated = true;} else {
								ErrorDialog.showErrorDialog(f, "Введите правильный номер телефона");
								validated = false;
							}	
						}else {
							if(carOwner == null) {
								ErrorDialog.showErrorDialog(f, "Введите имя владельца");
								validated = false;
							}else {
								validated = true;
							}
						}
					}
				}
				
				if(validated) {
					//if new car is successfully added to the db, update list	
					if(DBManager.addRegisteredCar(factory, f, carNumber, carModel, carOwner, ownerTel, true, ownerExists)) {
						carNumberText.setText(null);
						carModelText.setText(null);
						carOwnerText.setText(null);	
						ownerTelText.setText(null);
						parkedCarInfo.setText(null);
						registeredCarInfo.setText(null);
						title.setText(null);
						
						updateCarsList(factory, f, parkedCarsList, myParkedList, "parked");
						parkedNumbersModel.insertElementAt(carNumber, 0); 
						updateCarsList(factory, f, registeredCarsList, myRegisteredList, "registered");
						registeredNumbersModel.insertElementAt(carNumber, 0);
						updateOwnersList(factory, f, myOwnersList);
						ownerCarsModel.clear();
						ownerNamesModel.clear();
						for(int i=0; i<myOwnersList.size(); i++) {ownerNamesModel.addElement(myOwnersList.get(i).getOwnerName());}
					}	
				}
			}
		});
		
		registeredCarsList.addListSelectionListener((event)-> {
			int index = registeredCarsList.getSelectedIndex();
			String value = registeredCarsList.getSelectedValue();
			if(registeredNumbersModel.size() > 0 && registeredNumbersModel != null && index >= 0 && value != null) {
				if(!event.getValueIsAdjusting()) {
					Car selectedCar = myRegisteredList.get(index);
					String carModel = selectedCar.getCarModel()==null ? "не указана" : selectedCar.getCarModel();
					String ownerName = selectedCar.getCarOwner()==null ? "не указан" : selectedCar.getCarOwner().getOwnerName();
					String ownerTel;
					if(ownerName == "не указан") {
						ownerTel = "не указан";
					}else {
						ownerTel = selectedCar.getCarOwner().getOwnerTel()==null ? "не указан" : selectedCar.getCarOwner().getOwnerTel();
					}
					registeredCarInfo.setText("Марка/модель: "+carModel+"\nВладелец: "+ownerName+"\nТелефон: "+ownerTel);
				}else {registeredCarInfo.setText(null);}
			}	
		});
		
		updateRegisteredCar.addActionListener((event)-> {
			int index = registeredCarsList.getSelectedIndex();
			String value = registeredCarsList.getSelectedValue();
			if(index < 0) {
				ErrorDialog.showErrorDialog(f, "Выберите автомобиль из списка");
			}else {
				String tel = myRegisteredList.get(index).getCarOwner()==null ? null : myRegisteredList.get(index).getCarOwner().getOwnerTel();
				String name = myRegisteredList.get(index).getCarOwner()==null ? null : myRegisteredList.get(index).getCarOwner().getOwnerName();
				UpdateCarDialog dialog = new UpdateCarDialog(factory, f, myRegisteredList.get(index).getCarNumber(), 
							myRegisteredList.get(index).getCarModel(), name, tel,
							registeredCarsList, myRegisteredList, parkedCarsList, myParkedList, myOwnersList);
				dialog.setVisible(true);
				
				//update models (registered, parked, owners)
				registeredCarInfo.setText(null);
				parkedCarInfo.setText(null);
				title.setText(null);
				registeredNumbersModel.setElementAt(myRegisteredList.get(index).getCarNumber(), index);	
				for(int i=0; i<myParkedList.size(); i++) {
					if((myParkedList.get(i).getCarNumber()).equals(value)) {
						parkedNumbersModel.setElementAt(myParkedList.get(i).getCarNumber(), i);
						break;
					}		
				}
				ownerNamesModel.clear();
				ownerCarsModel.clear();
				for(int i=0; i<myOwnersList.size(); i++) {ownerNamesModel.addElement(myOwnersList.get(i).getOwnerName());}
			}
		});
		
		deleteRegisteredCar.addActionListener((event)-> {
			String carNumber = registeredCarsList.getSelectedValue();
			if(carNumber == null) {
				ErrorDialog.showErrorDialog(f, "Выберите автомобиль из списка");
			}else {
				Object[] options = {"Да","Нет"};
				int choice = JOptionPane.showOptionDialog(f, "Удалить выбранный автомобиль?", "Вы уверены?", 
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if(choice == 0) {	
					DBManager.deleteRegisteredCar(factory, f, carNumber);
					registeredCarInfo.setText(null);
					parkedCarInfo.setText(null);
					title.setText(null);

					if(myRegisteredList.get(registeredCarsList.getSelectedIndex()).getCarOwner() != null) {
						updateOwnersList(factory, f, myOwnersList);
						ownerNamesModel.clear();
						for(int i=0; i<myOwnersList.size(); i++) {ownerNamesModel.addElement(myOwnersList.get(i).getOwnerName());}
					}
					ownerCarsModel.clear();
						
					updateCarsList(factory, f, registeredCarsList, myRegisteredList, "registered");
					registeredNumbersModel.removeElement(carNumber);
						
					for(int i=0; i<myParkedList.size(); i++) {
						if((myParkedList.get(i).getCarNumber()).equals(carNumber)) {
							parkedNumbersModel.removeElement(myParkedList.get(i).getCarNumber());
							break;
						}		
					}
				}
			}
		});
		
		
		ownersList.addListSelectionListener((event)-> {
			int index = ownersList.getSelectedIndex();
			String value = ownersList.getSelectedValue();
			if(ownerNamesModel.size() > 0 && ownerNamesModel != null && index >= 0 && value != null) {
				if(!event.getValueIsAdjusting()) {
					CarOwner selectedOwner = myOwnersList.get(index);
					List<Car>cars = new ArrayList<>();
					cars.addAll(selectedOwner.getCars());
					ownerCarsModel.clear();
					for(int i=0; i<cars.size(); i++) {
							ownerCarsModel.addElement(cars.get(i).getCarNumber());
					}						
					ownerCarsList.setModel(ownerCarsModel);
					String ownerName = selectedOwner.getOwnerName();
					String ownerTel = selectedOwner.getOwnerTel()==null ? "телефон не указан" : selectedOwner.getOwnerTel();
					String titleStr = " "+ownerName+"\n "+ownerTel;
					title.setText(titleStr);
				}
			}
		});
		
		deleteOwner.addActionListener((event)-> {
			String ownerName = ownersList.getSelectedValue();
			if(ownerName == null) {ErrorDialog.showErrorDialog(f, "Выберите владельца из списка");
			}else {
				Object[] options = {"Да","Нет"};
				int choice = JOptionPane.showOptionDialog(f, "Удалить выбранного владельца?", "Вы уверены?", 
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if(choice == 0) {
					DBManager.deleteCarOwner(factory, f, ownerName);
					registeredCarInfo.setText(null);
					parkedCarInfo.setText(null);
					title.setText(null);
					UICreator.updateOwnersList(factory, f, myOwnersList);
					ownerNamesModel.removeElement(ownerName);
					ownerCarsModel.clear();
					parkedNumbersModel.clear();
					registeredNumbersModel.clear();
					for(int i=0; i<myRegisteredList.size(); i++) {registeredNumbersModel.addElement(myRegisteredList.get(i).getCarNumber());}
					for(int i=0; i<myParkedList.size(); i++) {parkedNumbersModel.addElement(myParkedList.get(i).getCarNumber());}
				}
			}
		});
		
		updateOwner.addActionListener((event)-> {
			String ownerName = ownersList.getSelectedValue();
			String ownerTel = myOwnersList.get(ownersList.getSelectedIndex()).getOwnerTel(); 
			if(ownerName == null) {ErrorDialog.showErrorDialog(f, "Выберите владельца из списка");
			}else {
				UpdateOwnerDialog dialog = new UpdateOwnerDialog(factory, f,  ownerName, ownerTel,
						registeredCarsList, myRegisteredList, parkedCarsList, myParkedList, myOwnersList);
				dialog.setVisible(true);
				//update models
				registeredCarInfo.setText(null);
				parkedCarInfo.setText(null);
				title.setText(null);
				ownerCarsModel.clear();
				registeredNumbersModel.clear();
				parkedNumbersModel.clear();
				ownerNamesModel.clear();
				for(int i=0; i<myRegisteredList.size(); i++) {registeredNumbersModel.addElement(myRegisteredList.get(i).getCarNumber());}
				for(int i=0; i<myParkedList.size(); i++) {parkedNumbersModel.addElement(myParkedList.get(i).getCarNumber());}
				for(int i=0; i<myOwnersList.size(); i++) {ownerNamesModel.addElement(myOwnersList.get(i).getOwnerName());}
					
			}
		});
		

		/////SWITCH TABS\\\\\
		
		//---CARS | OWNERS---\\
		registeredCarsLabel.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				registeredCarsLabel.setBackground(frontColor);
			}
			public void mouseExited(MouseEvent e) {
				registeredCarsLabel.setBackground(backColor);
			}
			public void mouseClicked(MouseEvent e) {
				registeredCards.show(registeredCont, "cars");
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		registeredOwnersLabel.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				registeredOwnersLabel.setBackground(frontColor);
			}
			public void mouseExited(MouseEvent e) {
				registeredOwnersLabel.setBackground(backColor);
			}
			public void mouseClicked(MouseEvent e) {
				registeredCards.show(registeredCont, "owners");
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		
		//----MAIN TABS-----\\
		parkedLabel.addMouseListener(new MouseListener(){
			@Override
			public void mouseEntered(MouseEvent e){
				parkedLabel.setBackground(frontColor);
			}
			public void mouseExited(MouseEvent e){
				parkedLabel.setBackground(backColor);
			}
			public void mouseClicked(MouseEvent e) {
				cards.show(contentPanel,"parked");
				//disable button "Выезд" in case parked cars list is empty
				if(parkedNumbersModel.getSize() > 0) {
					newCarOutButton.setEnabled(true);
				}else {newCarOutButton.setEnabled(false);}
				toGoLabel.setText(null);
			}	
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		registeredLabel.addMouseListener(new MouseListener(){
			@Override
			public void mouseEntered(MouseEvent e){
				registeredLabel.setBackground(frontColor);
			}
			public void mouseExited(MouseEvent e){
				registeredLabel.setBackground(backColor);
			}
			public void mouseClicked(MouseEvent e) {
				cards.show(contentPanel,"registered");
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		newCarLabel.addMouseListener(new MouseListener(){
			@Override
			public void mouseEntered(MouseEvent e){
				newCarLabel.setBackground(frontColor);
			}
			public void mouseExited(MouseEvent e){
				newCarLabel.setBackground(backColor);
			}
			public void mouseClicked(MouseEvent e) {
				cards.show(contentPanel,"newCar");
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});

		
		f.setVisible(true);
		//we close the window like this cause we have to close the SessionFactory before
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try{
					FactoryUtil.closeFactory();
				}catch(Exception exc) {}finally {
					System.exit(0);
				}
			}
		});	
	}
}
