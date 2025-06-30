package sec_addons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Class used to load a KeyStore file into code, so that it can be used
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class KeyStoreLoader {

    //Paths to the locations of various keystores, along with related info
    public static final String SERVER_TRUSTSTORE_PATH = "./sec_addons/keystores/truststore.server";
    public static final String SERVER_ALIAS = "myserver";
    public static final String SERVER_KEYSTORE_PASSWORD = "changeit";

    public static final String CLIENT_KEYSTORE_PATH = "./client_tools/keystores/";
    public static final String CLIENT_TRUSTSTORE_PATH = "./client_tools/keystores/truststore.client";
    
    /**
     * Method that loads a Keystore file into a KeyStore object, so that it may be used in 
     * future operations
     * @param keystoreFilePath path to the location of the keystore
     * @param password password of that keystore
     * @return a KeyStore object loaded from the keystore file
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     */
    public static KeyStore loadKeyStore(String keystoreFilePath, String password) throws KeyStoreException, IOException,
     NoSuchAlgorithmException, CertificateException {
        File keystoreFile = new File(keystoreFilePath);
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        FileInputStream input = new FileInputStream(keystoreFile);
        keystore.load(input, password.toCharArray());
        if (input != null) {
            input.close();
        }
        return keystore;
    }

    /**
     * Getter that returns the public key contained in a given keystore, belonging to a certain alias
     * @param keystore the keystore which stores the public key
     * @param alias the alias of the owner of the public key
     * @return the public key 
     * @throws KeyStoreException
     */
    public static PublicKey getPublicKey(KeyStore keystore, String alias) throws KeyStoreException {
        Certificate certificate = keystore.getCertificate(alias);
        PublicKey publicKey = certificate.getPublicKey();
        return publicKey;
    }
    
    /**
     * Getter that returns the private key contained in a given keystore, belonging to a certain alias
     * @param keystore the keystore which stores the private key
     * @param alias the alias of the owner of the private key
     * @param password the password of that keystore
     * @return the private key 
     * @throws KeyStoreException
     */
    public static PrivateKey getPrivateKey(KeyStore keystore, String alias, String password) throws KeyStoreException, 
    UnrecoverableKeyException, NoSuchAlgorithmException {
    	return (PrivateKey) keystore.getKey(alias, password.toCharArray());
    }
}