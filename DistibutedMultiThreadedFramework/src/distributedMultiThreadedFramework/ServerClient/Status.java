//$Id: Status.java 5928 2013-01-11 07:45:27Z ChristopherSmith $
package distributedMultiThreadedFramework.ServerClient;



/**
 * Client sends these Statuses
 * COMPLETE : all processing done on client side, reports success or failure 
 * ACTIVE   : ready to take input from server
 * PING     : checking in from client also makes sure client still alive
 * 
 * 
 * Server sends these Statuses
 * COMMAND  : send command parameters to client
 * SHUTDOWN : send no more commands to client, client shutdown gracefully
 * STOP     : halts execution, shutdown not guaranteed graceful
 * 
 */
public enum Status {
	COMPLETE, ACTIVE, PING, COMMAND, SHUTDOWN, STOP
}
