package xsipro;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginUI implements ActionListener {
	private JTextField userNameTf, userPassTf;
	private JButton loginBtn, resetBtn;
	private static String encodecredentials;

	public LoginUI() {
	
		JFrame frame = new JFrame("JFrame Example");
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JLabel userLabel = new JLabel("Username");
		userLabel.setBackground(Color.white);
		userLabel.setForeground(new Color(120, 120, 120));
		userNameTf = new JTextField(60);
		userNameTf.setText("rismy.cj@kakaposystems.com");
		JLabel passLabel = new JLabel("Password");
		passLabel.setForeground(new Color(120, 120, 120));
		userPassTf = new JPasswordField(60);
		userPassTf.setText("Rismy@Kakapo1");
		loginBtn = new JButton("LOGIN");
		resetBtn = new JButton("RESET");
		panel.add(userLabel);
		panel.add(userNameTf);
		panel.add(passLabel);
		panel.add(userPassTf);
		panel.add(loginBtn);
		panel.add(resetBtn);
		panel.setBackground(Color.BLACK);
		frame.add(panel);
		frame.setSize(800, 800);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().setBackground(Color.YELLOW);
		loginBtn.addActionListener(this);
		resetBtn.addActionListener(this);
		frame.show();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "LOGIN":
			String userName = userNameTf.getText();
			String paasValue = userPassTf.getText();
			System.out.println("userName=" + userName);
			System.out.println("paasValue=" + paasValue);
			String userCredentials = userName + ":" + paasValue;
			String encodedString = Base64.getEncoder().encodeToString(userCredentials.getBytes());
			encodecredentials = "Basic " + encodedString;
			ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
			Runnable createChannelTask = () -> {
				XSIChannelHandler handler = new XSIChannelHandler(userName, paasValue);
				handler.createChannel();
			};
			executorService.schedule(createChannelTask, 0, TimeUnit.SECONDS);

			break;
		case "RESET":
			userNameTf.setText("");
			userPassTf.setText("");
			break;
		default:

			break;
		}
		e.getActionCommand();

	}

}
