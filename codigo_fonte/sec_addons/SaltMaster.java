package sec_addons;
import java.util.Base64;
import java.util.Random;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class that generates a random salt, for it to be used in hashing operations
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class SaltMaster {
    
    /**
     * Method that generates a random salt
     * @return random array of bytes, with 24 bytes
     */
    public static byte[] giftSalt() {
        Random random = new Random();
        byte bytes[] = new byte[24];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Method that adds the salt to a password, so that it can be hashed
     * @param password password to hash
     * @param salt salt to add to it
     * @return a string containing the password and the salt
     * @throws IOException
     */
    public static byte[] addSaltToPassword(byte[] password, byte[] salt) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(password);
        outputStream.write(salt);
        return Base64.getEncoder().encode(outputStream.toByteArray());
    }
}
