package sec_addons;

import java.io.IOException;
import java.util.Base64;

/**
 * Class that stores one single instance of the Digester object, for it to be
 * used throughout the server's execution
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class RealDigester {
	
	//single instance of the Digester object
	public static Digester digester = new Digester();
	
	/**
	 * Method that hashes a password with a given salt
	 * @param password password to hash
	 * @param saltToUse salt to be used in the hashing
	 * @return a string with the hashed password
	 * @throws IOException
	 */
	public static String hashPasswordWithSalt(String password, String saltToUse) throws IOException {
		byte[] passwordBytes = password.getBytes();
        byte[] salt = Base64.getDecoder().decode(saltToUse);
        byte[] passAndSalt = SaltMaster.addSaltToPassword(passwordBytes, salt);
        byte[] hashedAndSalted = digester.hash(passAndSalt);
        return Base64.getEncoder().encodeToString(hashedAndSalted);
	}

	/**
	 * Method that hashes a password with a given salt in byte[] format
	 * @param password password to hash
	 * @param saltToUse salt to be used in the hashing
	 * @return a string with the hashed password
	 * @throws IOException
	 */
	public static String hashPasswordWithSalt(String password, byte[] salt) throws IOException {
		byte[] passwordBytes = password.getBytes();
        byte[] passAndSalt = SaltMaster.addSaltToPassword(passwordBytes, salt);
        byte[] hashedAndSalted = digester.hash(passAndSalt);
        return Base64.getEncoder().encodeToString(hashedAndSalted);
	}

	/**
	 * Method that returns a salt in byte[] format to a string format
	 * @param salt salt to convert
	 * @return salt in string format
	 */
	public static String encodeSaltIntoString(byte[] salt) {
		return Base64.getEncoder().encodeToString(salt);
	}
	
}
