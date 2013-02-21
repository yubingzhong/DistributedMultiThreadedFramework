//$Id: Main.java 5934 2013-01-11 12:46:20Z ChristopherSmith $
package distributedMultiThreadedFramework.Main;

import java.util.Scanner;


import distributedMultiThreadedFramework.MultiThreading.ExecutionType;
import distributedMultiThreadedFramework.MultiThreading.MultiThreading;
import distributedMultiThreadedFramework.PasswordCracking.CrackType;
import distributedMultiThreadedFramework.ServerClient.Client;
import distributedMultiThreadedFramework.ServerClient.Server;


/**
 * used for console commands only
 * 
 * @author smitc
 * @author olivb
 */
public class Main {

	private static boolean showDebugMessages = true;

	public static void main(String[] args) {
		//*
		Scanner scanner = new Scanner(System.in);

		while (true) {

			System.out.println("Server (s) or Client (c)? ");
			String sc = scanner.nextLine();

			if (sc.equalsIgnoreCase("s")) {

				String port;
				if(showDebugMessages) {
					port = "9999";
				}
				else {
					System.out.println("What port to listen on? ");
					port = scanner.nextLine();
				}

				 String [] params = {"PRPCHTTP","l","1","1000000","administrator@pega.com","lsmitcw7","9191","/prweb/PRServlet/!STANDARD?pyActivity=LogOff"};
				//String[] params = {"HASH","ln","1","50000000","MD5","b6906ee34260793abc1b89e841e4eaf4", ""};
				// String [] params = {"JDBC","lns","1","1000000","postgres","jdbc:postgresql://localhost:5432/postgres","org.postgresql.Driver",""};

				long startTime = System.currentTimeMillis();

				Server server = new Server(ExecutionType.BRUTEFORCE, Integer.parseInt(port), params, showDebugMessages);
				server.serverProcess();

				System.out.println("Total Time: " + (System.currentTimeMillis() - startTime) / 1000 + " Seconds");
				break;

			} else if (sc.equalsIgnoreCase("c")) {

				String port;
				String serverName;
				String serverport;
				if(showDebugMessages) {
					port = "8888";
					serverName = "lsmitcw7";
					//serverName = "lolivbw7";
					serverport = "9999";
				}
				else {
					System.out.println("What port to listen on? ");
					port = scanner.nextLine();
	
					System.out.println("What server to connect to? ");
					serverName = scanner.nextLine();
	
					System.out.println("What server port to connect to? ");
					serverport = scanner.nextLine();
				}

				Client client = new Client(Integer.parseInt(serverport), serverName, Integer.parseInt(port), showDebugMessages);
				client.clientProcess();
				break;
			}
		}
		//*/

		
//		 MultiThreading multiThread = new MultiThreading("HTTP", "l", BigInteger.valueOf(1),
//					BigInteger.valueOf(1000000), "user000@loadtest.com", null, 
//					null, null, "sdvpwin110", "9494", null, null, null, null);
		
		
//		 MultiThreading multiThread = new MultiThreading("JDBC", "luns", BigInteger.valueOf(1),
//				BigInteger.valueOf(1000000),"postgres", 
//				"jdbc:postgresql://localhost:5432/postgres","org.postgresql.Driver",
//				null, null, null, null, null, null, null);

		
//		String[] params = {CrackType.HASH.toString(),"chirst","1","1000000","MD5","6b34fe24ac2ff8103f6fce1f0da2ef57",""};
//		//String [] params = {CrackType.PRPCHTTP.toString(),"qwertyuiopalskdjfhgzmxncbv","1","1000","user000@loadtest.com","sdvpwin110","9797",""};
//		
//		
//		//for (int i = 8; i < 9; i++){
//		MultiThreading multiThread = new MultiThreading(ExecutionType.BRUTEFORCE, 0, params);
//				 
//		multiThread.multiCoreExecute();
//		
//		System.out.println(multiThread.result());
//		
//		System.out.println("Finished...");
	}
}
