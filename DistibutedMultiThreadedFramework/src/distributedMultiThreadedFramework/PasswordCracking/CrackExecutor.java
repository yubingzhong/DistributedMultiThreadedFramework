//$Id: CrackExecutor.java 5927 2013-01-11 03:55:12Z ChristopherSmith $
package distributedMultiThreadedFramework.PasswordCracking;


/**
 * Forces standardization of Crack Execution Types
 * 
 * @author smitc
 * @author olivb
 */
public abstract class CrackExecutor {
	
	//the attack execution starting point
	public abstract boolean crack(String password);
	
	//any necessary steps to reset the object (as it is resused between iterations per thread)
	public abstract void reset();
}
