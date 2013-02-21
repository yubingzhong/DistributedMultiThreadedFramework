//$Id: ClientApplet.java 5933 2013-01-11 12:43:16Z BenjaminOliver $
package distributedMultiThreadedFramework.GUI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import distributedMultiThreadedFramework.ServerClient.Client;
import distributedMultiThreadedFramework.ServerClient.ServerClientHelper;
import distributedMultiThreadedFramework.ServerClient.Status;


import java.awt.event.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * simple applet to allow other computers to run client code without the jar file
 * @author smitc
 * @author olivb
 *
 */
public class ClientApplet extends JApplet {

	private static final long serialVersionUID = -2632773243282087197L;
	private boolean debug = true;

	Dimension appletDimension = new Dimension(400,400);
	Dimension standardDimension = new Dimension(310, 150);
	Dimension labelDimension = new Dimension(50, 25);
	Dimension inputDimension = new Dimension(150, 25);
	Dimension buttonDimension = new Dimension(150, 30);
	
	
	JScrollPane scrollPane;
	JTextPane outputTextPane;
	StyledDocument styledDocument;
	Style style;

	JPanel titlePanel;
	JPanel inputPanel;
	JPanel buttonPanel;
	JPanel errorPanel;
	JPanel mainPanel;

	JLabel title;

	JLabel localPortLabel;
	JTextField localPortInput;

	JLabel serverNameLabel;
	JTextField serverNameInput;

	JLabel serverPortLabel;
	JTextField serverPortInput;

	String startButtonLabel = "Start";
	JButton startButton;
	
	String stopButtonLabel = "Stop";
	JButton stopButton;

	JLabel errorMessage;

	
	/**
	 * sets up the applet painting. 
	 */
	public void init() {

		// Set up Applet
		setSize(this.appletDimension);

		// Panels
		this.titlePanel = new JPanel();
		this.inputPanel = new JPanel();
		this.buttonPanel = new JPanel();
		this.errorPanel = new JPanel();
		this.mainPanel = new JPanel();

		// Title
		this.title = new JLabel("Client Parameters");
		this.titlePanel.setMaximumSize(this.standardDimension);
		
		// Add to titlePanel
		this.titlePanel.add(title);

		// Input Panel
		this.inputPanel.setLayout(new GridLayout(3, 2));
		this.inputPanel.setMaximumSize(this.standardDimension);

		// Button Panel
		this.buttonPanel.setMaximumSize(this.standardDimension);

		// Error Message
		this.errorPanel.setMaximumSize(this.standardDimension);
	
		
		// Local Port
		this.localPortLabel = new JLabel("Local Port: ");
		this.localPortLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.localPortLabel.setSize(this.labelDimension);
		this.localPortInput = new JTextField();
		if(this.debug)
			this.localPortInput = new JTextField("8888");
		this.localPortInput.setPreferredSize(this.inputDimension);

		// Add to inputPanel
		this.inputPanel.add(this.localPortLabel);
		this.inputPanel.add(this.localPortInput);

		
		// Server Name
		this.serverNameLabel = new JLabel("Server Name: ");
		this.serverNameLabel.setSize(this.labelDimension);
		this.serverNameInput = new JTextField();
		if(this.debug) {
			this.serverNameInput = new JTextField("lsmitcw7");
			//this.serverNameInput = new JTextField("lolivbw7");
		}
		this.serverNameInput.setPreferredSize(this.inputDimension);
		
		// Add to inputPanel
		this.inputPanel.add(this.serverNameLabel);
		this.inputPanel.add(this.serverNameInput);

		
		// Server Port
		this.serverPortLabel = new JLabel("Server Port: ");
		this.serverPortLabel.setSize(this.labelDimension);
		this.serverPortInput = new JTextField();
		if(this.debug)
			this.serverPortInput = new JTextField("9999");
		this.serverPortInput.setPreferredSize(this.inputDimension);
		
		// Add to inputPanel
		this.inputPanel.add(this.serverPortLabel);
		this.inputPanel.add(this.serverPortInput);


		// Buttons
		this.startButton = new JButton(this.startButtonLabel);
		this.startButton.setPreferredSize(this.buttonDimension);

		this.stopButton = new JButton(this.stopButtonLabel);
		this.stopButton.setPreferredSize(this.buttonDimension);
		this.stopButton.setVisible(false);

		// Add to buttonPanel
		this.buttonPanel.add(this.startButton);
		this.buttonPanel.add(this.stopButton);

		
		// Error Message
		this.errorMessage = new JLabel(" ");
		this.errorMessage.setForeground(Color.RED);
		
		// Add to errorPanel
		this.errorPanel.add(this.errorMessage);
		this.errorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		
		// Set up JTextPane: output
		this.outputTextPane = new JTextPane();
		
		// Set up Styles
		this.styledDocument = outputTextPane.getStyledDocument();

		style = outputTextPane.addStyle("ConsoleOutput", null);
        
        
        // Redirect System.out to the outputConsole 
//		System.setOut(new PrintStream(new OutputStream() {
//			public void write(int b) throws IOException {
//				StyleConstants.setForeground(style, Color.blue);
//				try { 
//					styledDocument.insertString(styledDocument.getLength(), ("" + (char)b), style); 
//				} catch (BadLocationException e) {
//				}
//			}
//		}));
		
		// Redirect System.err to the outputConsole
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) throws IOException {
				StyleConstants.setForeground(style, Color.red);
				try { 
					styledDocument.insertString(styledDocument.getLength(), ("" + (char)b), style); 
				} catch (BadLocationException e) {
				}
			}
		}));
	
		        
		// Set up JScrollPane
		this.scrollPane = new JScrollPane(this.outputTextPane);

		
		// Setup and Add panels to main panel
		this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
		this.mainPanel.add(this.titlePanel);
		this.mainPanel.add(this.inputPanel);
		this.mainPanel.add(this.buttonPanel);
		this.mainPanel.add(this.errorPanel);
		this.mainPanel.add(this.scrollPane);

		// Container = JApplet container
		Container window = getContentPane();
		window.setLayout(new GridLayout(1, 1));
		this.startButton.addActionListener(new ButtonListener());
		stopButton.addActionListener(new ButtonListener());
		
		// Add mainPanel to container: window
		window.add(this.mainPanel);
	}


	/**
	 * ingress into client code
	 * @param client
	 */
	private void clientRun(Client client){
		client.clientProcess();
	}


	/**
	 * override buttonlistener to detect a click 
	 * @author smitc
	 * @author olivb
	 */
	private class ButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent push) {
			String command = push.getActionCommand();
			
			// If 'Start' button is pressed
			if(command.equals(startButtonLabel)) {
				String localPort = localPortInput.getText();
				String serverName = serverNameInput.getText();
				String serverPort = serverPortInput.getText();
				int localPortNumber = -1;
				int serverPortNumber = -1;
				ServerSocket listenSocket = null;
				Socket socketToServer = null;
				
				// checking inputs 
				try {
					localPortNumber = Integer.parseInt(localPort);
				} catch (NumberFormatException e) {
					errorMessage.setText("Local Port is not a number");
					return;
				}

				try {
					serverPortNumber = Integer.parseInt(serverPort);
				} catch (NumberFormatException e) {
					errorMessage.setText("Server Port is not a number");
					return;
				}

				try {
					listenSocket = new ServerSocket(localPortNumber);
					listenSocket.close();
				} catch (Exception e) {
					errorMessage.setText("Could not open local port: " + localPort + ". Choose another.");
					return;
				} 

				try {
					// open connection to server
					InetAddress serverAddress = InetAddress.getByName(serverName);
					socketToServer = new Socket(serverAddress, serverPortNumber);
					
					// create initial connection
					OutputStream outputStream = socketToServer.getOutputStream();
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
					BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

					// report in
					bufferedWriter.write(ServerClientHelper.createPacket(Status.PING.toString(), localPort, "", ""));
					bufferedWriter.close();
					socketToServer.close();

				} catch (Exception e) {
					errorMessage.setText(e.getMessage());
					return;
				}
				errorMessage.setForeground(Color.BLUE);
				errorMessage.setText("Beginning Execution");
				startButton.setVisible(false);
				stopButton.setVisible(true);
				//TODO switch start to stop?
				
				Client client = new Client(serverPortNumber, serverName, localPortNumber, true);
				clientRun(client);
				errorMessage.setForeground(Color.BLUE);
				errorMessage.setText("Password Found!");
			}
			// If 'Stop' button is pressed
			else if(command.equals(stopButtonLabel)) {
				
			}
			else {
				System.err.println("Button is not recognized");
			}
			
		}
	}
}
