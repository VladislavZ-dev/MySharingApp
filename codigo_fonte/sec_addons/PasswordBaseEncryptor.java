package sec_addons;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class that performs all tasks needed for password based encryption
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class PasswordBaseEncryptor {

    /**
     * Method that generates a new symmetric key with a password as its base
     * @param password to generate the secret key
     * @return new secret key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey encryptWithPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = SaltMaster.giftSalt();
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = kf.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Method that generates a new symmetric key with a password as its base, for 
     * it to be used in MAC related operations
     * @param password to generate the secret key
     * @return new secret key
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey encryptMac(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = SaltMaster.giftSalt();
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        return kf.generateSecret(keySpec);
    }
}
