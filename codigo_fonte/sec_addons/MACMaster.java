package sec_addons;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import server_tools.FileScanner;

/**
 * Class that performs all tasks related to MAC
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class MACMaster {
    
    //executes MAC operations
    private Mac mac;

    //file that stores in text the MAC since the last operation
    private File fileWithMAC;

    //will be false if the server's administrator chose not to use a mac
    private boolean allowed;

    /**
     * Constructor. Initializes the MACMaster
     * @param fileWithMAC file where the mac will be written
     * @param allowed 
     * @throws NoSuchAlgorithmException
     */
    public MACMaster(File fileWithMAC, boolean allowed) throws NoSuchAlgorithmException {
        this.fileWithMAC = fileWithMAC;
        mac = Mac.getInstance("HmacSHA1");
        this.allowed = allowed;
    }

    /**
     * Method that sets the key for the Mac object to use
     * @param key key for the Mac object to use
     * @throws InvalidKeyException
     */
    public void setKey(Key key) throws InvalidKeyException {
        mac.init(key);
    }

    /**
     * Method that calculates the MAC of a certain data file, and then writes it into 
     * the file responsible for storing the MAC
     * @param dataFile file whose MAC will be calculated
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void writeMACIntoFile(File dataFile) throws IOException, NoSuchAlgorithmException {
        if (allowed) {
            byte[] userFileBytes = Files.readAllBytes(dataFile.toPath());
            mac.update(userFileBytes);
            MACWriter writer = new MACWriter(fileWithMAC);
            writer.writeMAC(getFinalMAC());
        }
    }

    /**
     * Method that calculates the MAC of a given set of data
     * @param data in byte[] format
     * @return the MAC of that data jn String format
     * @throws UnsupportedEncodingException
     * @throws IllegalStateException
     */
    public String getMACFromData(byte[] data) throws UnsupportedEncodingException, IllegalStateException {
        mac.update(data);
        return getFinalMAC();
    }

    /**
     * Method that reads the file which stores the MAC, and returns its content
     * @return the MAC written into the file 
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public String getExistingMAC() throws NoSuchAlgorithmException, IOException {
        FileScanner reader = new FileScanner(fileWithMAC);
        return reader.readFirstLine();

    }

    /**
     * Auxiliary method that calls the doFinal method of the Mac object, and returns 
     * the result in string format
     * @return
     * @throws UnsupportedEncodingException
     * @throws IllegalStateException
     */
    private String getFinalMAC() throws UnsupportedEncodingException, IllegalStateException {
        String finalMAC = Base64.getEncoder().encodeToString(mac.doFinal());
        return finalMAC;
    }

   
}
