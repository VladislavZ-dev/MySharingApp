import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import client_tools.*;

/**
 * Class that executes the client
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class MySharingClient {
    public static void main (String[] args) throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException,
     CertificateException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        if (args.length != 3) {
            System.out.println("O programa é corrido da forma:\n\tmySharingClient <serverAddress> <user-id> <password>");
            return;
        } 
        
        String serverAddress = args[0];
        String user = args[1];
        String password = args[2];

    	System.setProperty("javax.net.ssl.keyStore", "./client_tools/keystores/keystore." + user);
		System.setProperty("javax.net.ssl.keyStorePassword", password);
        System.setProperty("javax.net.ssl.trustStore", "./client_tools/keystores/truststore.client");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");


        Scanner scanner = new Scanner(System.in);
        ClientCommandHandler commander = null;
        try {
        	ClientSocketHandler.connectToServer(serverAddress);
        	commander = new ClientCommandHandler(user, password);
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        
        
        System.out.println("Cliente iniciou-se");
        try {
	        String authenticationStatus = commander.authenticate();
	        if(authenticationStatus.equals("WRONG-PWD")) {
	        	while(authenticationStatus.equals("WRONG-PWD")){
	            	System.out.println("A autenticação falhou.\nInsira de novo a Password: ");
	            	password = scanner.nextLine();
	            	commander.setPassword(password);
	            	authenticationStatus = commander.authenticateAgain();
	            }	
	        }
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
        	System.out.println("Erro a comunicar com o servidor!");
        }

        commander.printCommandsMenu();
        StringBuilder sb = new StringBuilder();
        Path path = Paths.get("");
        File directory = new File(path.toAbsolutePath().toString());

        for(String filename : directory.list()) {
        	if (new File(filename).isFile()) {
        		sb.append(filename);
        		sb.append(" ");		
        	}
        }

        System.out.println("\nUtilizador: " + user + "\nFicheiros existentes no cliente:" + sb.toString());
        
        while (true) {
            System.out.print("\nComando: ");
            String command = scanner.nextLine();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    ClientSocketHandler.disconnectFromServer();
                    scanner.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                	scanner.close();
                }
            }));
            try {
				commander.handleCommand(command);
			} catch (ClassNotFoundException | IOException | GeneralSecurityException e) {
				e.printStackTrace();
			}
        }
    }
}