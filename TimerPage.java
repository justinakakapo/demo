/*
 * package xsipro;
 * 
 * import javax.swing.*; import java.awt.event.ActionEvent; import
 * java.awt.event.ActionListener;
 * 
 * public class TimerPage {
 * 
 * 
 * public static void createAndShowGUI() { JFrame frame = new
 * JFrame("Timer Example");
 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 * 
 * JPanel panel = new JPanel(); JLabel timerLabel = new
 * JLabel("Timer: 0 seconds"); panel.add(timerLabel);
 * 
 * frame.add(panel); frame.setSize(300, 150); frame.setLocationRelativeTo(null);
 * frame.setVisible(true);
 * 
 * // Create a Timer with a 1-second delay Timer timer = new Timer(1000, new
 * ActionListener() { private int secondsElapsed = 0;
 * 
 * @Override public void actionPerformed(ActionEvent e) { // Update the timer
 * label every second timerLabel.setText("Timer: " + secondsElapsed +
 * " seconds"); secondsElapsed++; } });
 * 
 * // Start the Timer timer.start(); }
 * 
 * 
 * }
 */