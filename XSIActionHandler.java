package xsipro;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.JFrame;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XSIActionHandler extends JFrame {

	private static final String DELETE_URL = "https://xsp.insmartcloud.com/com.broadsoft.xsi-actions/v2.0/user/%s/calls/%s";
	private static final String POST_URL = "https://xsp.insmartcloud.com/com.broadsoft.xsi-actions/v2.0/user/%s/calls/new";
	private static final String RETRIEVE_URL = "https://xsp.insmartcloud.com/com.broadsoft.xsi-actions/v2.0/user/%s/calls/%s/Talk";
	private static StringBuffer response;
	private static String encodecredentials;
	private static String callId;
	private static String userId;

	public XSIActionHandler() {

	}

	public void dialRequest(String dailing_text, String username, String encodecredentials2) throws IOException {
		userId = username;
		encodecredentials = encodecredentials2;
		String uri = String.format(POST_URL, userId);
		String resultUri = uri + "?address=" + dailing_text;
		System.out.println("final=" + resultUri);
		HttpURLConnection con = (HttpURLConnection) new URL(resultUri).openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", encodecredentials);
		con.setDoOutput(true);
		try (DataOutputStream outputStream = new DataOutputStream(con.getOutputStream())) {
			outputStream.writeBytes(dailing_text);
			outputStream.flush();
		}
		int responseCode = con.getResponseCode();
		System.out.println("dialRequest(), " + responseCode + " : " + con.getResponseMessage());
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = "";
			response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println(response);
		} else {
			System.out.println("dialRequest(), failed with error code:" + responseCode);
		}
		xmlParsing(response.toString());
	}

	private static void xmlParsing(String callResponse) {

		Document doc = XMLUtil.convertStringToXMLDocument(callResponse);
		System.out.println("Root Node : " + doc.getFirstChild().getNodeName());
		NodeList nodeList = doc.getElementsByTagName("CallStartInfo");
		for (int itr = 0; itr < nodeList.getLength(); itr++) {
			Node node = nodeList.item(itr);
			System.out.println("\nNode Name : " + node.getNodeName());
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				System.out.println("callId: " + eElement.getElementsByTagName("callId").item(0).getTextContent());
				callId = eElement.getElementsByTagName("callId").item(0).getTextContent();
			}
		}

	}

	public static void retrieveRequest(String dailing_text, String username, String encodecredentials2)
			throws IOException {
		System.out.println("dailing_text::" + callId);
		String retrieveUrl = String.format(RETRIEVE_URL, userId, callId);
		System.out.println("in retrieve url" + retrieveUrl);
		URL obj = new URL(retrieveUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("PUT");
		con.setRequestProperty("Authorization", encodecredentials);
		int responseCode = con.getResponseCode();
		System.out.println("retrieveRequest() Response Code :: " + responseCode + " " + con.getResponseMessage());
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {// success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println("RetrieveRequest(): " + response);
		} else {
			System.out.println("RetrieveRequest() failed, response code:" + responseCode);
		}
	}

	public void rejectRequest(String dailing_text, String username, String encodecredentials2) throws IOException {

		userId = username;
		encodecredentials = encodecredentials2;
		String rejectUrl = String.format(DELETE_URL, userId, callId);
		URL obj = new URL(rejectUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("DELETE");
		con.setRequestProperty("Authorization", encodecredentials);
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("rejectRequest() Response Code :: " + responseCode + " " + con.getResponseMessage());
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println(response.toString());
		} else {
			System.out.println("RejectRequest() request did not work." + responseCode);
		}
	}

	public void holdRequest(String dailing_text, String username, String encodecredentials2) throws IOException {

		userId = username;
		encodecredentials = encodecredentials2;
		callId = URLEncoder.encode(callId);
		System.out.println("userId==" + userId);
		System.out.println("encodecredentials==" + encodecredentials);
		System.out.println("callId==" + callId);
		String sf1 = String.format("https://xsp.insmartcloud.com/com.broadsoft.xsi-actions/v2.0/user/%s/calls/%s/Hold",
				userId, callId);
		URL obj = new URL(sf1);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("PUT");
		con.setRequestProperty("Authorization", encodecredentials);
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("holdRequest() Response Code :: " + responseCode + " " + con.getResponseMessage());
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println(response);
		} else {
			System.out.println("holdRequest()  did not work with responsecode." + responseCode);
		}
	}
}
