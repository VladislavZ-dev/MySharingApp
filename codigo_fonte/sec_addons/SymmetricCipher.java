package sec_addons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * Class used to perform actions related with symmetric encryption, mostly
 * using the AES algorithm
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class SymmetricCipher {
    
    /**
	 * Starts a cipher with the AES algorithm ready to be used for encryption
	 * @param key AES key used for encryption
	 * @return the AES cipher ready to be used
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
    public static Cipher startEncryptCipher(Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
    	Cipher AESCipher = Cipher.getInstance("AES");
        AESCipher.init(Cipher.ENCRYPT_MODE, key);
        return AESCipher;
    }

    /**
	 * Starts a cipher with the AES algorithm ready to be used for decryption
	 * @param key AES key used for decryption
	 * @return the AES cipher ready to be used
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
    public static Cipher startDecryptCipher(Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher AESCipher = Cipher.getInstance("AES");
	    AESCipher.init(Cipher.DECRYPT_MODE, key);
	    return AESCipher;
	}

    /**
     * Method that either encrypts or decrypts a source file, and stores the result in a 
     * destination file
     * @param source the source file to be encrypted/decrypted
     * @param destination the destination file that stores the encrypted/decrypted bytes
     * @param cipher the cipher that encrypts/decrypts
     * @throws IOException
     */
    public static void cipher(File source, String destination, Cipher cipher) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(destination);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        
        byte[] buffer = new byte[1024];  
        int i = fis.read(buffer);
        while (i != -1) {
            cos.write(buffer, 0, i);
            i = fis.read(buffer);
        }
        
        fis.close();
        cos.close();
    }
}
