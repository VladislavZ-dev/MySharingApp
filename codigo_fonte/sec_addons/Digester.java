package sec_addons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class used to digest a given set of data
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class Digester{

    //tool that performs the hash/digest
    private MessageDigest digester;

    /**
     * Contructor. Initializes the digester with the SHA-256 algorithm, which
     * is considered safe
     */
    public Digester() {
        try {
			this.digester = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    }

    /**
     * Method that takes a set of data and returns its hash
     * @param messageBytes data in byte[] format
     * @return the hash of that data, in byte[] format
     */ 
    public byte[] hash(byte[] messageBytes){
        return digester.digest(messageBytes);
    }
}