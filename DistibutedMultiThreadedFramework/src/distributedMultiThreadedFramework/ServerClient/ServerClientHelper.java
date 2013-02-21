//$Id: ServerClientHelper.java 5934 2013-01-11 12:46:20Z ChristopherSmith $
package distributedMultiThreadedFramework.ServerClient;

import java.util.Arrays;


/**
 * general helpful code for both client and server
 * 
 * @author smitc
 *
 */
public class ServerClientHelper {

	static String standardDelimeter = "\n";
	static String contentDelimeter = ";;";

	/**
	 * creates a packet to send to another machine
	 * 
	 * @param status the status of the current machine
	 * @param localHostPort port this message will be sent from
	 * @param message the message to pass
	 * @return string containing the full message
	 */
	public static String createPacket(String status, String localHostPort, String message, String content) {

		StringBuilder sb = new StringBuilder();
		sb.append("status:");
		sb.append(status);

		sb.append(standardDelimeter);
		sb.append("localhostport:");
		sb.append(localHostPort);

		sb.append(standardDelimeter);
		sb.append("message:");
		sb.append(message);
		
		sb.append(standardDelimeter);
		sb.append("content:");
		sb.append(content);

		sb.append("\n\n");

		return sb.toString();
	}


	/** 
	 * given a packet, parse into pieces
	 * 
	 * @param packetContents string of the full message from another machine
	 * @return String array of status, localhostport, and message
	 */
	public static String[] parsePacket(String packetContents) {

		//fills array with blank strings to make sure blank strings pass through to client processes
		String[] parsed = new String[15];
		Arrays.fill(parsed, "");

		String[] split = packetContents.split(standardDelimeter);

		for (int i = 0; i < Math.min(parsed.length, split.length); i++)
			parsed[i] = split[i];

		parsed[0] = parsed[0].replace("status:", "");
		parsed[1] = parsed[1].replace("localhostport:", "");
		parsed[2] = parsed[2].replace("message:", "");
		parsed[3] = parsed[3].replace("content:", "");

		return parsed;
	}
	
	public static String[] splitContents(String contents){
		String[] parsed = new String[contents.length()];
		
		int i = 0;
		int index = 0;
		while(contents.length() > 0){
			index = contents.indexOf(contentDelimeter);
			parsed[i++] = contents.substring(0, index);
			contents = contents.substring(index + contentDelimeter.length());
		}
		String [] content = new String[i];
		for(int j = 0; j < content.length; j++)
			content[j] = parsed[j];
		return content;
	}
}





































