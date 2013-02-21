//$Id: HashCracker.java 5935 2013-01-11 12:48:01Z ChristopherSmith $
package distributedMultiThreadedFramework.PasswordCracking;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;


/**
 * cracks hashes based on algorithm type
 * 
 * @author smitc
 * @author olivb
 */
public class HashCracker extends CrackExecutor {

	private String algorithm = null;
	private String hash = null;
	private byte[] salt = null;
	private MessageDigest messageDigest = null;

	
	/**
	 * 
	 * @param algorithm: one of MD5, SHA-1, SHA-2 SHA-256, SHA-512 etc. Must be algorithm that MessageDigest knows
	 * @param hash: the hex string, 0 padded hash
	 * @param optionalSalt: if hash is padded with any salt, add it here
	 */
	public HashCracker(String algorithm, String hash, String optionalSalt) {
		this.algorithm = algorithm;
		this.hash = hash;
		
		try {
			this.messageDigest = MessageDigest.getInstance(this.algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		if (optionalSalt != null && optionalSalt.trim().length() != 0)
			this.salt = optionalSalt.getBytes();	
	}


	/**
	 * creates a hash of type algorithm possibly adding the salt 
	 * 
	 * @param password: the current password guess
	 * @return: true if the hashes match. false otherwise
	 */
	@Override
	public boolean crack(String password) {
		byte[] output = this.messageDigest.digest(password.getBytes());
		
		if (this.salt != null && this.salt.length != 0) {
			this.messageDigest.update(this.salt);
			output = this.messageDigest.digest();
		}
		if (password.contains("chris"))
			System.out.println();
//		StringBuilder sb = new StringBuilder();
//	    for (byte b : output) {
//	        sb.append(String.format("%1$02x", b));
//	    }
//	    String finalOut = sb.toString();
		
		String finalOut = new String(Hex.encodeHex(output));
		return finalOut.contentEquals(this.hash);
	}

	
	/**
	 * resets the message digest, without recreating the object
	 */
	@Override
	public void reset() {
		this.messageDigest.reset();
	}
}
