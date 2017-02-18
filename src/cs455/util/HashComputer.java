package cs455.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashComputer {
	
	public String SHA1FromBytes(byte[] data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(data);
			BigInteger hashInt = new BigInteger(1, hash);
			
			return hashInt.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		}
		
		return null;
	}

}
