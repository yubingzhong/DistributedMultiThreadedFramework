//$Id: Client.java 5934 2013-01-11 12:46:20Z ChristopherSmith $
package distributedMultiThreadedFramework.ServerClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import distributedMultiThreadedFramework.MultiThreading.ExecutionType;
import distributedMultiThreadedFramework.MultiThreading.MultiThreading;


/**
 * Client code to connect to and receive commands from a server
 * 
 * @author smitc
 * @author olivb
 */
public class Client {
	private MultiThreading multiThread;
	
	// to accept messages
	private ServerSocket listenSocket = null;
	private int listenPort = -1;

	// to connect to a server
	private Socket socketToServer = null;
	private InetAddress serverAddress = null;
	private String serverHost = null;
	private int serverPort = -1;
	
	private int numOfThreads;
	
	private boolean shutdown;

	/**
	 * 
	 * @param serverPort: remote server port
	 * @param serverHost: remote server name
	 * @param listenPort: local client listening port
	 * @param debugMessages: whether to output debug or not
	 */
	public Client(int serverPort, String serverHost, int listenPort, boolean debugMessages) {
		if (serverPort < 1)
			serverPort = 9090;
		if (listenPort < 1)
			listenPort = 9191;
		this.serverPort = serverPort;
		this.serverHost = serverHost;
		this.listenPort = listenPort;
		
		this.numOfThreads = 0;	// 0 or default (automatically find out)
		this.shutdown = false;
	}


	/**
	 * ingress to manage all client functions
	 */
	public void clientProcess() {

		try {
			// open port to be able to listen
			this.listenSocket = new ServerSocket(this.listenPort);
			System.out.println("Server started. Listening on port: " + this.listenPort);

			// open connection to server
			this.serverAddress = InetAddress.getByName(this.serverHost);
			this.socketToServer = new Socket(this.serverAddress, this.serverPort);

			// create initial connection
			OutputStream outputStream = this.socketToServer.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			// report in
			bufferedWriter.write(ServerClientHelper.createPacket(Status.ACTIVE.toString(), String.valueOf(this.listenPort), "", ""));
			bufferedWriter.close();

			while (!this.shutdown) {
				this.socketToServer = this.listenSocket.accept();
				InputStream inputStream = this.socketToServer.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				StringBuffer stringBuffer = new StringBuffer();
				
				String line = "";
				while((line = bufferedReader.readLine()) != null && line.trim().length() != 0){
					stringBuffer.append(line);
					stringBuffer.append("\n");
				}
				
				this.socketToServer.close();

				String[] packetParams = ServerClientHelper.parsePacket(stringBuffer.toString());
				String[] commandParams = null;
				
				// Check status received from server
				switch(Status.valueOf(packetParams[0])) {
					case COMMAND:
						commandParams = parseCommand(packetParams[3]);
						
						// Create multiThreader
						this.multiThread = new MultiThreading(ExecutionType.valueOf(packetParams[2]), this.numOfThreads, commandParams);
						
						// Run multiThreader
						this.multiThread.multiCoreExecute();

						if (this.multiThread.isFound()) 
							System.out.println("Password is: " + this.multiThread.result());
						else 
							System.out.println("Could not find password; replying to server");
						
						sendResults(Status.COMPLETE, this.multiThread.result(), commandParams[2], commandParams[3]);
											
						break;
					
					case SHUTDOWN:
						this.shutdown = true;
						break;
												
					case STOP:
						stopProcesses();
						break;
						
					default:
						System.err.println("Client does not understand this Status");
						break;
				}
			}

		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException caught in Client Code: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IOException caught in Client Code: " + e.getMessage());
		} finally {

		}
	}

	// parses content
	private String[] parseCommand(String command) {

		String[] split = ServerClientHelper.splitContents(command);

		return split;
	}
	
	
	// returns content
	private String createResult(String min, String max) {
		return min + ServerClientHelper.contentDelimeter + max;
	}
	
	
	public void stopProcesses() {
		this.shutdown = true;
		this.multiThread.shutdown();
		//TODO
	}
	
	
	/**
	 * send final results to server
	 * 
	 * @param status success of fail
	 * @param result password or guesses
	 */
	private void sendResults(Status status, String password, String min, String max) {
		try {
			this.socketToServer = new Socket(this.serverAddress, this.serverPort);
			// create initial connection
			OutputStream outputStream = this.socketToServer.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			bufferedWriter.write(ServerClientHelper.createPacket(status.toString(), String.valueOf(this.listenPort), password, createResult(min, max)));
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
