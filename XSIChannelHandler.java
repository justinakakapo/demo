package xsipro;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XSIChannelHandler extends JFrame implements ActionListener {

	private JTextField dailing_textfield = new JTextField(16);
	private JTextField callstatus_textfield = new JTextField(16);
	private String username;
	private String password;
	private String encodecredentials;
	private String subscriptionId, channelId;
	private static String dailing_text;
	JFrame frame;
	JButton answerBtn, callBtn, holdBtn, rejectBtn, retieveBtn;
	private String responseFromChannel;
	public static JLabel timerLabel = new JLabel("Timer: 0 seconds");
	static Timer timer;
	private static final String heartbeatXML = "<ChannelHeartBeat xmlns=\"http://schema.broadsoft.com/xsi\"/>";
	private static final String xmlMeta = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private static String dailingNum;
	XSIActionHandler XSIActionHandler = new XSIActionHandler();

	public XSIChannelHandler(String userName, String paasValue) {
		this.username = userName;
		this.password = paasValue;
		this.encodecredentials = "Basic " + Base64.getEncoder().encodeToString((userName + ":" + paasValue).getBytes());

	}

	public XSIChannelHandler() {

	}

	public void createChannel() {
		System.out.println("in create channel");
		try {
			String url = "https://xsp.insmartcloud.com/com.broadsoft.xsi-events/v2.0/channel";
			URL apiUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", encodecredentials);

			String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Channel xmlns=\"http://schema.broadsoft.com/xsi\"><channelSetId>ChannelSetA</channelSetId><priority>1</priority><weight>50</weight><expires>3600</expires></Channel>";

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = xmlData.getBytes("UTF-8");
				os.write(input);
				os.flush();
				os.close();
			} catch (Exception ex) {
				System.out.println("Exception in OS :" + ex);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// Reading the response
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					responseFromChannel = "";
					int i = 0;
					while ((i = in.read()) != -1) {
						char ch = (char) i;
						responseFromChannel += ((char) ch);
						manageResponse();
					}
					System.out.println("Out of while");
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Invalid ", "Warning", JOptionPane.WARNING_MESSAGE);
				System.out.println("createChannel(), request failed with response code: " + responseCode);
			}
			connection.disconnect();
		} catch (Exception e) {
		
                JOptionPane.showMessageDialog(frame, "Invalid ", "Warning", JOptionPane.WARNING_MESSAGE);
            
			e.printStackTrace();
		}
	}

	private boolean isValidLogin(Exception e) {
		// TODO Auto-generated method stub
		return false;
	}

	private void manageResponse() {

		if (responseFromChannel.endsWith("</Channel>")) {
			System.out.println("response::" + responseFromChannel);
			channelId = "";
			Document doc = XMLUtil.convertStringToXMLDocument(responseFromChannel);
			if (doc != null) {
				channelId = doc.getElementsByTagName("channelId").item(0).getTextContent();
				System.out.println("channelId==" + channelId);
			}
			responseFromChannel = "";
			ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
			Runnable subscriptionTask = () -> {
				subscription();
			};
			executorService.schedule(subscriptionTask, 0, TimeUnit.SECONDS);
			Runnable heartbeatTask = () -> {
				sendHeartbeat(channelId, encodecredentials);
			};
			executorService.scheduleAtFixedRate(heartbeatTask, 0, 2, TimeUnit.SECONDS);
			System.out.println("dialer ui");
			createdailUI();
		} else if (responseFromChannel.endsWith("</xsi:Event>")) {
			responseFromChannel = responseFromChannel.replaceAll("<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?>",
					"");
			responseFromChannel = responseFromChannel.replaceAll(heartbeatXML, "");
			if (responseFromChannel.contains("xsi:CallOriginatingEvent")) {
				//createAndShowGUI();
				 timer = new Timer(1000, new ActionListener() {
					private int secondsElapsed = 0;

					@Override
					public void actionPerformed(ActionEvent e) {
						// Update the timer label every second
						timerLabel.setText("Timer: " + secondsElapsed + " seconds");
						secondsElapsed++;
					}
				});

				// Start the Timer
				timer.start();
			
				System.out.println("call started");
				callstatus_textfield.setText("call started");
			} else if (responseFromChannel.contains("xsi:CallReleasedEvent")) {
				System.out.println("call disconnected");
				timer.stop();
				callstatus_textfield.setText("call disconnected");
			} else if (responseFromChannel.contains("xsi:CallAnsweredEvent")) {
				System.out.println("call connected");
				callstatus_textfield.setText("call connected");
			}
			System.out.println("XSI event:" + responseFromChannel);
			responseFromChannel = "";
		}
	}

	private void createdailUI() {

		 callBtn = new JButton("call");
		 rejectBtn = new JButton("reject");
		 holdBtn = new JButton("hold");
		 retieveBtn = new JButton("retrieve");
		JLabel callstatus = new JLabel("Call Status");
		// JLabel timerLabel = new JLabel("Timer: 0 seconds");

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(dailing_textfield);
		panel.add(callBtn);
		panel.add(rejectBtn);
		panel.add(holdBtn);
		panel.add(retieveBtn);
		panel.add(callstatus);
		panel.add(callstatus_textfield);
		panel.add(timerLabel);

		 frame = new JFrame("caller");
		frame.add(panel);
		frame.setSize(500, 500);
		frame.show();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		callBtn.addActionListener(this);
		rejectBtn.addActionListener(this);
		holdBtn.addActionListener(this);
		retieveBtn.addActionListener(this);
		callBtn.setEnabled(true);
		holdBtn.setEnabled(false);
		retieveBtn.setEnabled(false);
		rejectBtn.setEnabled(false);

	}

	public void actionPerformed(ActionEvent e) {
		dailing_text = dailing_textfield.getText();

		String s = e.getActionCommand();
		if ((s.charAt(0) >= '0' && s.charAt(0) <= '9')) {
			dailingNum = dailing_text;
		} else if (s == "call") {
			if (dailing_text.equals("")) {
				dailing_textfield.setText("enter number");
			} else {
				try {
					XSIActionHandler.dialRequest(dailing_text, username, encodecredentials);
					callBtn.setEnabled(false);
					holdBtn.setEnabled(true);
					retieveBtn.setEnabled(true);
					rejectBtn.setEnabled(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else if (s == "reject") {
			try {
				XSIActionHandler.rejectRequest(dailing_text, username, encodecredentials);
				callBtn.setEnabled(true);
				holdBtn.setEnabled(false);
				retieveBtn.setEnabled(false);
				rejectBtn.setEnabled(false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (s == "hold") {
			try {
				XSIActionHandler.holdRequest(dailing_text, username, encodecredentials);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (s == "retrieve") {
			try {
				XSIActionHandler.retrieveRequest(dailing_text, username, encodecredentials);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void createAndShowGUI() {
		/*
		 * JFrame frame = new JFrame("Timer Example");
		 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 * 
		 * JPanel panel = new JPanel(); JLabel timerLabel = new
		 * JLabel("Timer: 0 seconds"); panel.add(timerLabel);
		 * 
		 * frame.add(panel); frame.setSize(300, 150); frame.setLocationRelativeTo(null);
		 * frame.setVisible(true);
		 */

		// Create a Timer with a 1-second delay
		Timer timer = new Timer(1000, new ActionListener() {
			private int secondsElapsed = 0;

			@Override
			public void actionPerformed(ActionEvent e) {
				// Update the timer label every second
				timerLabel.setText("Timer: " + secondsElapsed + " seconds");
				secondsElapsed++;
			}
		});

		// Start the Timer
		timer.start();
	}

	private void subscription() {

		try {
			System.out.println("in sub");
			URL apiUrl = new URL(
					String.format("https://xsp.insmartcloud.com/com.broadsoft.xsi-events/v2.0/user/%s", username));
			HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", encodecredentials);
			String xmlData1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
					+ "<Subscription xmlns=\"http://schema.broadsoft.com/xsi\">"
					+ "<targetIdType>User</targetIdType><event>Standard Call</event>"
					+ "<expires>3600</expires><channelSetId>ChannelSetA</channelSetId>"
					+ "<applicationId>SomeApplicationId1</applicationId></Subscription>";
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = xmlData1.getBytes("UTF-8");
				os.write(xmlData1.getBytes("UTF-8"));
				os.flush();
				os.close();
			} catch (Exception ex) {
				System.out.println("Exception :" + ex);
			}
			int responseCode = connection.getResponseCode();
			System.out.println("sub responseCode from server:\n" + responseCode);
			String responseString = "";
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
						responseString = response.toString();
					}
					System.out.println("sub response from server:\n" + response.toString());
					Document doc = XMLUtil.convertStringToXMLDocument(responseString);
					System.out.println("Root Node : " + doc.getFirstChild().getNodeName());
					NodeList nodeList = doc.getElementsByTagName("Subscription");
					for (int itr = 0; itr < nodeList.getLength(); itr++) {
						Node node = nodeList.item(itr);
						System.out.println("\nNode Name : " + node.getNodeName());
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) node;
							String subscriptionId = eElement.getElementsByTagName("subscriptionId").item(0)
									.getTextContent();
							System.out.println("subscriptionId: " + subscriptionId);
						}
					}
				}
				connection.disconnect();
			} else {
				System.out.println("sub POST request failed with response code: " + responseCode);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendHeartbeat(String channelId, String encodecredentials2) {

		try {
			String url = "https://xsp.insmartcloud.com/com.broadsoft.xsi-events/v2.0/channel/%s/heartbeat";
			String sf2 = String.format(url, URLEncoder.encode(channelId));

			URL obj = new URL(sf2);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("PUT");
			con.setRequestProperty("Authorization", encodecredentials);
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}