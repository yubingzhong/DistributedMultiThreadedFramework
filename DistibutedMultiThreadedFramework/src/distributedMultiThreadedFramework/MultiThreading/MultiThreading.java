//$Id: MultiThreading.java 5931 2013-01-11 12:17:02Z BenjaminOliver $
package distributedMultiThreadedFramework.MultiThreading;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import distributedMultiThreadedFramework.Tasks.BruteForcer;
import distributedMultiThreadedFramework.Tasks.MultiThreadedTask;


/**
 * executes an ExecutionType on Tasks using one or more threads simultaneously
 * 
 * @author smitc
 * @author olivb
 */
public class MultiThreading {
	final ExecutorService exec;
	private int numOfThreads;
	
	//these are pushed to tasks
	String[] params = null;
	ExecutionType executionType = null;
	String[] result = null;

	
	public MultiThreading(ExecutionType executionType, int numOfThreads, String[] params){
		this.numOfThreads = numOfThreads;
		//if bad value given, default to number of currently available CPU cores
		if (this.numOfThreads <= 0)
			this.numOfThreads = Runtime.getRuntime().availableProcessors();

		this.exec = Executors.newFixedThreadPool(this.numOfThreads);
		
		this.params = params;
		this.executionType = executionType;
		this.result = new String[] {""};
	}

	
	/**
	 * creates tasks by ExecutionType
	 * 
	 * @param currentThreadIndex: the thread number
	 * @param numThreads: total number of threads
	 * @param latches: ready, start, and done latches for control flow in multiThreading
	 * @param exec: the executor service handling all threads
	 * @return: a new Task based on the ExecutionType 
	 */
	//only place to add new params
	private MultiThreadedTask determineTaskType(int currentThreadIndex, CountDownLatch[] latches){
		switch(this.executionType){
			case BRUTEFORCE:
				return new BruteForcer(this.params, this.result, currentThreadIndex, this.numOfThreads, latches, this.exec);
			case DICTIONARY:
				System.out.println("Currently Unsupported Execution Type");
				return null;
			case RAINBOW:
				System.out.println("Currently Unsupported Execution Type");
				return null;
			default:
				System.err.println("Unsupported Execution Type");
				return null;
				
		}
	}
	
	
	/**
	 * primary ingress into this class. provides time statistics on crack time spent
	 * @param cores number of cores to use
	 */
	public void multiCoreExecute() {
		System.out.println("Starting Crack");
		double time = generateThreads();
		System.out.println(time / (1000000000)); //output in seconds
	}


	/**
	 * generates and executes threads to run the tasks.
	 * 
	 * @param threads: number of cores to user (defaults to available processors if given <1)
	 * @return: the total time it takes to finish in nano seconds
	 */
	private long generateThreads(){
		
		System.out.println("Using " + this.numOfThreads + " threads for processing");

		//these are all referenced by each thread so changes in one thread affect all threads
		final CountDownLatch readyLatch = new CountDownLatch(this.numOfThreads);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch doneLatch = new CountDownLatch(this.numOfThreads);
		
		for (int i = 0; i < this.numOfThreads; i++){
			final int currentThreadIndex = i;
			this.exec.execute(determineTaskType(currentThreadIndex, new CountDownLatch[] {readyLatch, startLatch, doneLatch}));
		}

		//generate time statistics, starting all threads
		long startTime = 0;
		try{
			readyLatch.await();
			startTime = System.nanoTime();
			startLatch.countDown();
			
			//once all threads terminate properly this completes
			doneLatch.await();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		
		return System.nanoTime() - startTime;
	}
	
	public String result(){
		return this.result[0];
	}
	
	public boolean isFound() {
		return this.result[0].length() != 0;
	}
	
	public void shutdown() {
		this.exec.shutdownNow();
	}
}
