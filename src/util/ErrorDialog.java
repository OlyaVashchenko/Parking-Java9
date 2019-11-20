package util;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ErrorDialog {
	public static void showErrorDialog(JFrame frame, String message) {
		JOptionPane.showMessageDialog(frame, message, "Возникла ошибка", JOptionPane.ERROR_MESSAGE);
	}
}
