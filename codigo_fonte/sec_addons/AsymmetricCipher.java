package sec_addons;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class used to perform actions related with asymmetric encryption, mostly
 * using the RSA algorithm
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class AsymmetricCipher {
    
	/**
	 * Starts a cipher with the RSA algorithm ready to be used for encryption
	 * @param key RSA key used for encryption
	 * @return the RSA cipher ready to be used
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
    public static Cipher startEncryptCipher(Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher RSACipher = Cipher.getInstance("RSA");
        RSACipher.init(Cipher.ENCRYPT_MODE, key);
        return RSACipher;
    }

	/**
	 * Starts a cipher with the RSA algorithm ready to be used for decryption
	 * @param key RSA key used for decryption
	 * @return the RSA cipher ready to be used
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
    public static Cipher startDecryptCipher(Key key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher RSACipher = Cipher.getInstance("RSA");
        RSACipher.init(Cipher.DECRYPT_MODE, key);
        return RSACipher;
    }

	/**
	 * Method that safely encrypts data for it to be sent over the network
	 * @param cipher cipher that encrypts
	 * @param bytesToSend bytes of the data to be sent
	 * @return the encrypted bytes of the original data
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	public static byte[] encryptBytesToSend(Cipher cipher, byte[] bytesToSend) throws IllegalBlockSizeException, BadPaddingException, IOException {
		
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	int chunkSize = 245;
		
    	for (int offset = 0; offset < bytesToSend.length; offset += chunkSize) {
    	    int end = offset + chunkSize;
    	    if (end > bytesToSend.length) {
    	        end = bytesToSend.length;
    	    }
    	    byte[] chunk = Arrays.copyOfRange(bytesToSend, offset, end);
    	    byte[] encryptedChunk = cipher.doFinal(chunk);
    	    outputStream.write(encryptedChunk);
    	}

    	return outputStream.toByteArray();
	}
    
	/**
	 * Method responsible for generating the file with the workspace's secret key, 
	 * encrypted with the public key of one of its users
	 * @param workspacePath the path to the workspace where the file will be stored
	 * @param workspace the name of the workspace
	 * @param user the user whose public key will be used
	 * @param workspaceKey the secret key symmetric key of the workspace
	 * @param publicKey the public key of the user
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    public static void createWorkspaceEncryptedFile(String workspacePath, String workspace, String user, SecretKey workspaceKey, PublicKey publicKey)
	throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, FileNotFoundException, IOException {
		Cipher cipher = startEncryptCipher(publicKey);
		File workspaceCipherFile = new File(workspacePath + "a.txt");
		workspaceCipherFile.createNewFile();

		FileOutputStream fileOut = new FileOutputStream(workspaceCipherFile);
		fileOut.write(workspaceKey.getEncoded());
		fileOut.close();

		FileInputStream fis = new FileInputStream(workspaceCipherFile);
		FileOutputStream fos = new FileOutputStream(new File(workspacePath + workspace + ".key." + user));
		CipherOutputStream cipherOut = new CipherOutputStream(fos, cipher);
		byte[] bytes = new byte[16];  
		int i = fis.read(bytes);
		while (i != -1) {
		    cipherOut.write(bytes, 0, i);
		    i = fis.read(bytes);
		}

		cipherOut.close();
		fis.close();
		fos.close();
		new File(workspacePath + "a.txt").delete();
	}
    
	/**
	 * Method responsible for decrypting the file with the workspace's secret key,
	 * thus revealing it
	 * @param workspacePath path to where the file with the key is located
	 * @param user the name of the user whose private key will be used to decrypt the 
	 * 				workspace key file
	 * @param workspace the name of the workspace
	 * @param cipher the cipher used for decryption. Must use the private key of the user
	 * @return the secret key of the workspace revealed
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
    public static SecretKeySpec decryptWorkspaceFile(String workspacePath, String user, String workspace, Cipher cipher)
			throws FileNotFoundException, IOException, IllegalBlockSizeException, BadPaddingException {
		FileInputStream fis = new FileInputStream(workspacePath + workspace + ".key." + user);
		CipherInputStream cipherIn = new CipherInputStream(fis, cipher);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int bytesRead;
		while ((bytesRead = cipherIn.read(data)) != -1) {
		    buffer.write(data, 0, bytesRead);
		}
		cipherIn.close();
		buffer.flush();
		byte[] keyBytes = buffer.toByteArray();
		return new SecretKeySpec(keyBytes, "AES");
	}

	/**
	 * Method responsible for decrypting the file which contains a signature of a certain file, so 
	 * that its integrity and authenticity may be confirmed
	 * @param fileName the file that stores the signature
	 * @param cipher cipher used for decrypting. Must use the public key of its author
	 * @return the signature in a string format
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static String decryptSignature(String fileName, Cipher cipher) throws IOException, GeneralSecurityException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] encryptedBytes = Files.readAllBytes(Paths.get(fileName));
		for (int i = 0; i < encryptedBytes.length; i+=256) {
			int end = i + 256;
    	    if (end > encryptedBytes.length) {
    	        end = encryptedBytes.length;
    	    }
			byte[] chunk = Arrays.copyOfRange(encryptedBytes, i, end);
			byte[] encryptedChunk = cipher.doFinal(chunk);
			outputStream.write(encryptedChunk);
		}
		return Base64.getEncoder().encodeToString(outputStream.toByteArray());
	}
}
