//$Id: BruteForcer.java 5931 2013-01-11 12:17:02Z BenjaminOliver $
package distributedMultiThreadedFramework.Tasks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import distributedMultiThreadedFramework.PasswordCracking.CrackExecutor;
import distributedMultiThreadedFramework.PasswordCracking.CrackType;
import distributedMultiThreadedFramework.PasswordCracking.HashCracker;
import distributedMultiThreadedFramework.PasswordCracking.JDBCCracker;
import distributedMultiThreadedFramework.PasswordCracking.PRPCHTTPCracker;

public class BruteForcer extends MultiThreadedTask implements Runnable {

	private CountDownLatch readyLatch = null;
	private CountDownLatch startLatch = null;
	private CountDownLatch doneLatch = null;
	private ExecutorService exec = null;
	private CrackExecutor crackExecutor = null;
	private String[] crackedPassword = null;
	
	private BigInteger min = null;
	private BigInteger max = null;
	private BigInteger currentGuessNum = null;
	private BigInteger threads = null;
	private BigInteger offset = null;
	
	private int count;
	
	private char[] charSet = null; // Character Set
	private List<Character> currentGuess = null; // Current Guess
	
	//TODO Keep this/ vv
	/**
	 * 
	 * 
	 * @param crackType one of the implemented bruteforce use-cases (HASH, JDBC, HTTP)
	 * @param charSets any combination of "l" "u" "n" "s" for lower, upper, numbers, symbols
	 * @param start initial number to begin crack at where 1= first guess etc
	 * @param size the number of guesses to make in order until giving up
	 * @param usrname a userame is required for JDBC and HTTP cracking
	 * @param jdbc required for JDBC cracking (is the full jdbc path)
	 * @param clsName class name of the jdbc jar to use
	 * @param SQLStatemnt any sql statement to execute against a database. optional.
	 * @param URLname starting URL to run HTTP crack against
	 * @param prt port to run HTTP crack against
	 * @param algo algorithm to run on a hash
	 * @param hsh the hash value to crack
	 * @param slt optional salt to add to a hash
	 * @param cnsl the JTextArea console of the client applet
	 */


	
	public BruteForcer(String[] params, String[] crackedPassword, int currentThreadIndex, int numThreads, CountDownLatch[] latches, ExecutorService exec){
		this.readyLatch = latches[0];
		this.startLatch = latches[1];
		this.doneLatch = latches[2];
		this.exec = exec;
		this.crackedPassword = crackedPassword;
		this.offset = BigInteger.valueOf(currentThreadIndex);
		this.threads = BigInteger.valueOf(numThreads);
		
		this.count = 0;
		
		
		this.charSet = params[1].toCharArray(); 				//[1] string of charset 
		Arrays.sort(this.charSet);
		this.min = new BigInteger(params[2]);					//[2] starting numeric value
		this.max = new BigInteger(params[3]).add(this.min);		//[3] number of guesses to make
		this.currentGuessNum = this.min.add(this.offset);
		
		switch(CrackType.valueOf(params[0])){					//[0] type of CrackType
			case JDBC:
				if (params.length == 8)
					this.crackExecutor = new JDBCCracker(params[4], params[5], params[6], params[7]);
																//[4]username 
																//[5]JDBCURL
																//[6]JDBC driver class name 
																//[7]statement to make if successfully connected
				else
					System.err.println("Incorrect number of parameters for JDBC Cracking");
				break;
				
			case HASH:
				if (params.length == 7)
					this.crackExecutor = new HashCracker(params[4], params[5], params[6]);
																//[4]algorithm
																//[5]hashed value
																//[6]optional salt value 
				else
					System.err.println("Incorrect number of parameters for HASH Cracking");
				break;
				
			case PRPCHTTP:
				if (params.length == 8)
					this.crackExecutor = new PRPCHTTPCracker(params[4], params[5], params[6], params[7]);
																//[4]username 
																//[5]hostname 
																//[6]port number
																//[7]PRPC activity
				else
					System.err.println("Incorrect number of parameters for HTTP Cracking");
				break;
				
			case HTTP:
				System.err.println("pure HTTP unsupported at this time");
				break;
				
			default:
				System.err.println("Unsupported Crack Type");
				break;
		}
		
		this.currentGuess = createCharArray(this.min, this.charSet);
	}
	
	
	public void run(){
		this.readyLatch.countDown();
		try{
			this.startLatch.await();
			String attempt;

			while(this.crackedPassword[0].length() == 0){
				attempt = this.toString();

				if(!increment())		
					break;
				
				else if (this.crackExecutor.crack(attempt))
					this.crackedPassword[0] = attempt;
				
				this.crackExecutor.reset();
			}

		} catch (Exception e){
			e.printStackTrace();
			Thread.currentThread().interrupt();
		} finally{
			this.doneLatch.countDown();
			this.exec.shutdown();
		}
	}
	
	


	/**
	 * increments the guess by the least significant character
	 * @return true if incremented successfully, false if the max was hit
	 */
	public boolean increment() {		
		// Increment count
		this.count++;
		
		//int division = (int) (curIter * 100 / this.total.longValue());


		/*if (this.offset.compareTo(BigInteger.ONE) == 0 && this.percent / 10 < division / 10 && division <= 100) {
			this.percent = division;
			System.out.println(this.percent + "% complete of " + this.total + " guesses. Trying: " + stringifyCharArray(this.cg));
			if (console != null)
				console.append(this.percent + "% complete of " + this.total + " guesses. Trying: " + stringifyCharArray(this.cg) + "\n");
		}*/

		if (this.currentGuessNum.compareTo(this.max) > 0) 
			return false;
		
		// Ping Server after 1 million iterations
		if(this.count > 1000000) {
			this.count = 0;
			
			//Ping server
			
		}
		
		this.currentGuess = this.createCharArray(this.currentGuessNum, this.charSet);
		
		//currentGuessNum + num threads
		this.currentGuessNum = this.currentGuessNum.add(this.threads);
		
		return true;
	}

	
	/**
	 * creates a character array from a char set and value such that
	 * 
	 * charset = "abc" value 0 = a, value 1 = a, value 2 = b, value 4 = aa etc
	 * 
	 * @param value the number representation of the array to create
	 * @param charSet the available characters to create the array from
	 * @return character list array of the complete character array
	 */
	private List<Character> createCharArray(BigInteger value, char[] charSet) {
		List<Character> charArray = new ArrayList<Character>();

		BigInteger csSize = BigInteger.valueOf(charSet.length);

		if (value.compareTo(BigInteger.ZERO) == 0)
			charArray.add(0, charSet[0]);

		else {
			BigInteger[] divmod = value.divideAndRemainder(csSize);
			BigInteger modded = divmod[1];
			BigInteger digit = divmod[0];
			
			
			while (modded.compareTo(BigInteger.ZERO) != 0 || digit.compareTo(BigInteger.ZERO) != 0) {
				if (modded.compareTo(BigInteger.ZERO) == 0) {
					charArray.add(0, charSet[csSize.subtract(BigInteger.ONE).intValue()]);
					value = value.subtract(BigInteger.ONE);
				} else
					charArray.add(0, charSet[modded.subtract(BigInteger.ONE).intValue()]);

				value = value.divide(csSize);
				divmod = value.divideAndRemainder(csSize);
				modded = divmod[1];
				digit = divmod[0];
			}
		}

		return charArray;
	}


	/**
	 * 
	 * @return the cracked password if found or nothing otherwise
	 */
	public String result() {
		return this.crackedPassword[0];
	}
	
	
	/**
	 * overridden returns string representation
	 */
	public String toString() {
		return this.stringifyCharArray(this.currentGuess);
	}


	/**
	 * creates string representation of List char array
	 * @param array array of characters to become a string
	 * @return string
	 */
	public String stringifyCharArray(List<Character> array) {
		StringBuilder sb = new StringBuilder();

		for (Character s : array) 
			sb.append(s);

		return sb.toString();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

