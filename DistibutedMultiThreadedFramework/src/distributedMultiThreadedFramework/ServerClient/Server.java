//$Id: Server.java 5934 2013-01-11 12:46:20Z ChristopherSmith $
package distributedMultiThreadedFramework.ServerClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.SortedMap;
import java.util.TreeMap;

import distributedMultiThreadedFramework.MultiThreading.ExecutionType;


/**
 * Server implementation to send commands to clients and receive output from each 
 * 
 * @author smitc
 * @author olivb
 * 
 */
public class Server {
	private boolean debug = false;

	// stores what clients have. ClientString, port, starting guess value
	private SortedMap< String, Pair<String, BigInteger> > clientTracker = null;
	
	BigInteger currentGuessMax = null;
	private String crackedPassword = null;

	private String charSet = null;
	private BigInteger startValue = null;
	private BigInteger increment = null;

	private String[] parameters = null;
	private int port = -1;
	private boolean shutdown = false;
	private ExecutionType executionType = null;

	
	/**
	 * 
	 * @param port: port to listen on
	 * @param params: parameters for the multicracker function
	 * @param debugMessages: whether to output messages or not
	 */
	public Server(ExecutionType executionType, int port, String[] params, boolean debugMessages) {
		this.debug = debugMessages;
		this.parameters = params;
		
		if (port < 1)
			port = 9090;
		this.port = port;
		this.charSet = createCharacterSet(params[1]);
		this.startValue = new BigInteger(params[2]);
		this.increment = new BigInteger(params[3]);
		this.currentGuessMax = this.startValue;
		this.executionType = executionType;
		this.clientTracker = new TreeMap<String, Pair<String, BigInteger>>();
	}


	/**
	 * main server driver function to start socket listening and handle clients
	 */
	public void serverProcess() {
		try {
			// open socket to be able to listen
			ServerSocket serverSocket = new ServerSocket(this.port);

			System.out.println("Server started. Listening on port: " + this.port);

			while (!shutdown || !this.clientTracker.isEmpty()) {
				// accept incoming on this port
				Socket clientSocket = null;
				clientSocket = serverSocket.accept();
				handleClient(clientSocket);
			}

		} catch (IOException e) {
			System.err.println("IOException caught in Server Code: " + e.getMessage());
		}
	}


	/**
	 * handles client message 
	 * 
	 * @param message: the full message split into sections
	 * @param clientName: the client the message originates from
	 */
	private void handleMessage(String[] message, InetAddress clientName) {
		if (this.shutdown) {
			sendShutdownToClient(clientName.getCanonicalHostName());
		}

		switch(Status.valueOf(message[0])){
			case PING:
				System.out.println("Ping received from: " + clientName.getCanonicalHostName());
				break;
				
			case COMPLETE:
				// if failed to find a password
				if (message[2].length() == 0){
					System.out.println("Client has finished processing job");

					if (this.debug)
						System.out.println("Client reports these guesses: " + parseResult(message[3]));

					sendNextCommand(clientName, message[1]);
				}
				// password found
				else{
					System.out.println("Client found the password. Stopping all clients");

					this.crackedPassword = message[2];
					this.shutdown = true;
					System.out.println("Password=" + this.crackedPassword);
					sendShutdownToClient(clientName.getCanonicalHostName());
				}
					
				break;
				
			case ACTIVE:
				System.out.println("Client is Ready");
				sendNextCommand(clientName, message[1]);
				break;
				
			default:
				System.err.println("Server does not understand this status");
				
		}
	}


	/**
	 * gets the guesses made by a client
	 * 
	 * @param message: full text of message
	 * @return: the guesses only
	 */
	private String parseResult(String message) {
		String[] split = message.split(ServerClientHelper.contentDelimeter);
		
		return split[0] + " to " + split[1];
	}


	/**
	 * sends a command to a client to process the next batch of password guesses
	 * 
	 * @param clientName: name of client to send message to
	 * @param clientPort: port on client
	 */
	private void sendNextCommand(InetAddress clientName, String clientPort) {

		try {
			Socket clientSocket = new Socket(clientName, Integer.parseInt(clientPort));
			OutputStream outputStream = clientSocket.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			// create message
			String msg = createCommand();

			// send message packet
			bufferedWriter.write(ServerClientHelper.createPacket(Status.COMMAND.toString(), String.valueOf(this.port), this.executionType.toString(), msg.toString()));
			bufferedWriter.flush();

			if (!this.clientTracker.containsKey(clientName.getCanonicalHostName())) {
				this.clientTracker.put(clientName.getCanonicalHostName(), new Pair<String, BigInteger>(clientPort, this.currentGuessMax));
			}

			if (this.debug)
				System.out.println("Sent message to client " + clientSocket.getInetAddress().getCanonicalHostName() + " containing: " + msg.toString());

			// increment the currentGuessMax for the next client
			this.currentGuessMax = this.currentGuessMax.add(this.increment);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * reads message from client
	 * 
	 * @param clientSocket contains all client info and messages
	 */
	private void handleClient(Socket clientSocket) {
		InetAddress clientName = null;

		StringBuffer stringBuffer = new StringBuffer();	
		try {

			// remember client
			clientName = clientSocket.getInetAddress();

			// Prepare for message
			InputStream inputStream = clientSocket.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			String line = "";
			while((line = bufferedReader.readLine()) != null && line.trim().length() != 0){
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}

			if (this.debug)
				System.out.println("Received Message from client: " + clientName.getCanonicalHostName() + " \nContaining: " + stringBuffer.toString());

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		handleMessage(ServerClientHelper.parsePacket(stringBuffer.toString()), clientName);
	}


	/**
	 * sends shutdown to client
	 * 
	 * @param clientName: client to send message to
	 */
	private void sendShutdownToClient(String clientName) {
		try {

			String clientPort = this.clientTracker.get(clientName).getFirst();

			Socket clientSocket = new Socket(clientName, Integer.parseInt(clientPort));
			OutputStream outputStream = clientSocket.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			// send message packet
			bufferedWriter.write(ServerClientHelper.createPacket(Status.SHUTDOWN.toString(), String.valueOf(this.port), "", ""));
			bufferedWriter.flush();

			if (this.debug)
				System.out
				.println("Sent message to client " + clientSocket.getInetAddress().toString()
						+ "\nStatus: shutdown");

			this.clientTracker.remove(clientName);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private String createCharacterSet(String charOptions){
		String characters = "";
		
		if (charOptions.toUpperCase().contains("N"))
			characters += "0123456789";

		if (charOptions.toUpperCase().contains("L"))
			characters += "abcdefghijklmnopqrstuvwxyz";

		if (charOptions.toUpperCase().contains("U"))
			characters += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		if (charOptions.toUpperCase().contains("S"))
			characters += " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
					
		return characters;
	}
	
	
	

	private String createCommand() {
		
		this.parameters[1] = this.charSet;
		this.parameters[2] = this.currentGuessMax.toString();
		
		StringBuilder content = new StringBuilder();
		for(String param : parameters){
			content.append(param);
			content.append(ServerClientHelper.contentDelimeter);
		}
		
		return content.toString();
	}
	
	
	
	

	/**
	 * Pair class to handle client data
	 * @author smitc
	 *
	 * @param <A> left side parameter
	 * @param <B> right side parameter
	 */
	public class Pair<A, B> {
		private A first;
		private B second;

		public Pair(A first, B second) {
			super();
			this.first = first;
			this.second = second;
		}

		public int hashCode() {
			int hashFirst = first != null ? first.hashCode() : 0;
			int hashSecond = second != null ? second.hashCode() : 0;

			return (hashFirst + hashSecond) * hashSecond + hashFirst;
		}

		public boolean equals(Object other) {
			if (other instanceof Pair) {
				Pair<?, ?> otherPair = (Pair<?, ?>) other;
				return ((this.first == otherPair.first 
						|| (this.first != null 
						&& otherPair.first != null 
						&& this.first .equals(otherPair.first))) 
						&& (this.second == otherPair.second 
						|| (this.second != null 
						&& otherPair.second != null 
						&& this.second.equals(otherPair.second))));
			}

			return false;
		}

		public String toString() {
			return "(" + first + ", " + second + ")";
		}

		public A getFirst() {
			return first;
		}

		public B getSecond() {
			return second;
		}

	}
}
