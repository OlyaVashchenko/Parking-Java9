package logic;
/*
 * class to set JList foreground color: blue for registered cars and gray for unregistered
 */
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

class BlueGrayCellRenderer extends DefaultListCellRenderer{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Color frontColor;
	private Vector<String> carNumbers = new Vector<>();
	private List<Car> myParkedList = new ArrayList<>();
	public BlueGrayCellRenderer(List<Car> myParkedList, Color color){
		this.myParkedList = myParkedList;
		myParkedList.forEach(car -> carNumbers.add(car.getCarNumber()));
		this.frontColor = color;
	}
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
      Component component = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
      if(myParkedList.get(index).getRegistered() == true) {
      	component.setForeground(frontColor);
      }else {
      	component.setForeground(Color.GRAY);
      }
      return component;
  }
}