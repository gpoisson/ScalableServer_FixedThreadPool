package cs455.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashComputer {
	
	// Takes a byte array as input and returns an integer hash value using the SHA1 algorithm
	public String SHA1FromBytes(byte[] data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(data);
			BigInteger hashInt = new BigInteger(1, hash);
			
			return String.format("%40s", hashInt.toString(16)).replaceAll(" ", "0");
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		}
		
		return null;
	}

}
