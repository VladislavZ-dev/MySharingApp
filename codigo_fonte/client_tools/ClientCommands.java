package client_tools;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import file_communication.FileTransferer;
import sec_addons.*;
/**
 * Class responsible for the logic of the commands
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class ClientCommands {
	private Socket CLIENT_SOCKET = ClientSocketHandler.getSocket();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String user;
	private String password;
	
	/**
	 * Constructor. Initializes the class
	 * 
	 * @param user user currently sending commands
	 * @param password password of the current user
	 * @throws IOException
	 */
	public ClientCommands(String user, String password) throws IOException {
		this.user = user;
		this.password = password;
		this.in = new ObjectInputStream(CLIENT_SOCKET.getInputStream());
		this.out = new ObjectOutputStream(CLIENT_SOCKET.getOutputStream());
	}

	/**
	 * Method that tries to authenticate the user to the server
	 * 
	 * @param user 
	 * @param password password of the user
	 * @return OK if the password is correct, WRONG-PWD otherwise
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public String authenticate(String user, String password) throws IOException, ClassNotFoundException {
		String response = null;
		sendToServer(user);
		sendToServer(password);
		response = receiveMessage();
		return response;
	}
	
	/**
	 * Method that tries to authenticate the user after failed attempt
	 * 
	 * @param user 
	 * @param password password of the user
	 * @return OK if the password is correct, WRONG-PWD otherwise
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public String authenticateAgain(String password) throws IOException, ClassNotFoundException {
		String response = null;
		sendToServer(password);
		response = receiveMessage();
		return response;
	}
	
	/**
	 * Method that receives a message from the server
	 * 
	 * @return the message received
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public String receiveMessage() throws ClassNotFoundException, IOException {
		String message = null;
		message = (String) in.readObject();
		return message;
	}
	
	/**
	 * Method that sends the message to the server
	 * 
	 * @param message the message to be sent
	 * @throws IOException
	 */
	public void sendToServer(String message) throws IOException {
		out.writeObject(message);
	}
	
	/**
	 * Method that sends a request to create the workspace
	 * 
	 * @param workspace workspace to be created
	 * @return OK if the workspace was created, NOK otherwise
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
    public String createWorkspace(String workspace, String password) throws ClassNotFoundException, IOException {
		sendToServer("CREATE " + workspace + " " + password);
		return receiveMessage(); 	
    }
    
    /**
     * Method that sends a request to add a member to the workspace
     * 
     * @param user member to be added
     * @param workspace workspace which the member will be added
     * @return OK if the user was added,
     * 		   NOPERMS if the current user is not the owner of the workspace,
     * 		   NOWS if the workspace does not exist and
     * 		   NOUSER if the member to be added does not exist
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws CertificateException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyStoreException 
     * @throws UnrecoverableKeyException 
     * @throws NoSuchPaddingException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public String addMembers(String user, String workspace) throws ClassNotFoundException, IOException, KeyStoreException, 
	NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, 
	IllegalBlockSizeException, BadPaddingException {
		sendToServer("ADD " + user + " " + workspace);
    	String message = receiveMessage();
		if (message.equals("NOUSER"))
			return message;

    	String currentUserKeyFile = "./client_tools/" + workspace + ".key." + this.user;
    	FileTransferer.receiveFile(currentUserKeyFile, in);  
		
    	KeyStore keyStore = KeyStoreLoader.loadKeyStore(KeyStoreLoader.CLIENT_KEYSTORE_PATH + "keystore." + this.user, password);
    	PrivateKey privateKey = KeyStoreLoader.getPrivateKey(keyStore, this.user, password);
    	Cipher decipher = AsymmetricCipher.startDecryptCipher(privateKey);
    	SecretKey secKey = AsymmetricCipher.decryptWorkspaceFile("./client_tools/", this.user, workspace, decipher);

    	KeyStore keyStoreOtherUser = KeyStoreLoader.loadKeyStore(KeyStoreLoader.CLIENT_TRUSTSTORE_PATH, 
			KeyStoreLoader.SERVER_KEYSTORE_PASSWORD);
    	String newUserKeyFile = "./client_tools/" + workspace + ".key." + user;
		
    	AsymmetricCipher.createWorkspaceEncryptedFile("./client_tools/", workspace, user, secKey, 
    	    KeyStoreLoader.getPublicKey(keyStoreOtherUser, user));
		
    	FileTransferer.sendFile(newUserKeyFile, out);

    	new File(currentUserKeyFile).delete();
    	new File(newUserKeyFile).delete();
    
    	return message;
    }
    
    /**
     * Method that sends a request to uploads files to the workspace
     * 
     * @param workspace workspace which the files will be uploaded to
     * @param files files to be uploaded
     * @return NOPERM if the user is not a member of the workspace,
     * 		   NOWS if the workspace does not exist.
     * 
     * If the current user has permission and the workspace exists,
     * for every file:
     * 		   OK if the file was uploaded,
     * 		   Não existe if the file does not exist
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws CertificateException 
     * @throws NoSuchAlgorithmException 
     * @throws KeyStoreException 
     * @throws UnrecoverableKeyException 
     * @throws NoSuchPaddingException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public String uploadFiles(String workspace, String files) throws ClassNotFoundException, IOException, 
    KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, InvalidKeyException, 
    NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String response = null;
        StringBuilder sb = new StringBuilder();
		String[] singularFiles = files.split(",");
		String updatedFiles = arrayToString(singularFiles);
		String[] readyToGoFiles = updatedFiles.split(",");

		sendToServer("UP " +  workspace + " " + updatedFiles);
		response = receiveMessage();
		if (!response.equals("OK"))
			return response;
		
		FileTransferer.receiveFile("./client_tools/" + workspace + ".key." + user, in);

		KeyStore keyStore = KeyStoreLoader.loadKeyStore(KeyStoreLoader.CLIENT_KEYSTORE_PATH + "keystore." + user, password);
		PrivateKey privateKey = KeyStoreLoader.getPrivateKey(keyStore, user, password);
		
		Cipher decipher = AsymmetricCipher.startDecryptCipher(privateKey);
		SecretKey workspaceKey = AsymmetricCipher.decryptWorkspaceFile("./client_tools/",user, workspace, decipher);

		Cipher privateKeyCipher = AsymmetricCipher.startEncryptCipher(privateKey);
		Cipher workspaceKeyCipher = SymmetricCipher.startEncryptCipher(workspaceKey);
		
		for (String file : readyToGoFiles) {
			if (!file.contains("/")) {
				File inputFile = new File(file);
	        	byte[] fileBytes = Files.readAllBytes(inputFile.toPath());
				File signature = new File(file + ".signed");
				signature.createNewFile();

				byte[] hashedSignature = RealDigester.digester.hash(fileBytes);
				 
	        	byte[] encryptedSignature = AsymmetricCipher.encryptBytesToSend(privateKeyCipher, hashedSignature);
				Files.write(signature.toPath(), encryptedSignature);

				SymmetricCipher.cipher(inputFile, file + ".cif", workspaceKeyCipher);

		        FileTransferer.sendFile(file + ".cif", out);
				FileTransferer.sendFile(signature.getAbsolutePath(), out);
				deleteTemporaryFiles(signature.getName(), file + ".cif", "./client_tools/" + workspace + ".key." + user);
			}
			new File("./client_tools/" + workspace + ".key." + user).delete();
			response = receiveMessage();
			sb.append(response);
			sb.append("\n          ");
		}
        return sb.toString();
    }
    
    /**
     * Method that sends a request to download files from the workspace
     * 
     * @param workspace workspace which the files will be downloaded from
     * @param files files to be downloaded
     * @return NOPERM if the user is not a member of the workspace,
     * 		   NOWS if the workspace does not exist.
     * 
     * If the current user has permission and the workspace exists,
     * for every file:
     * 		   #ficheiro transferido if the file was uploaded,
     * 		   Não existe no workspace indicado if the file does not exist
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    public String downloadFiles(String workspace, String files) throws ClassNotFoundException, IOException, 
    GeneralSecurityException {
        String response = null;
        StringBuilder sb = new StringBuilder();
		String[] singularFiles = files.split(",");
		sendToServer("DW " +  workspace + " " + files);
		response = receiveMessage();
		if (!response.equals("OK"))
			return response;
		
		FileTransferer.receiveFile("./client_tools/" + workspace + ".key." + user, in);
		KeyStore keyStore = KeyStoreLoader.loadKeyStore(KeyStoreLoader.CLIENT_KEYSTORE_PATH + "keystore." + user, password);
		PrivateKey privateKey = KeyStoreLoader.getPrivateKey(keyStore, user, password);
		Cipher decipher = AsymmetricCipher.startDecryptCipher(privateKey);
		SecretKey secKey = AsymmetricCipher.decryptWorkspaceFile("./client_tools/", user, workspace, decipher);
		
		Cipher symmetricDecipher = SymmetricCipher.startDecryptCipher(secKey);
		
		for(String file : singularFiles) {
			response = receiveMessage();
			sb.append(response);
			sb.append("\n          ");
			if (response.equals(file + " #ficheiro transferido")) {
				String signatureOwner = receiveMessage();
				File signature = FileTransferer.receiveFile(file + ".signature", in);
				File encryptedFile = FileTransferer.receiveFile(file + ".cif", in);

				KeyStore truststore = KeyStoreLoader.loadKeyStore(KeyStoreLoader.CLIENT_TRUSTSTORE_PATH, KeyStoreLoader.SERVER_KEYSTORE_PASSWORD);
				PublicKey publicKey = KeyStoreLoader.getPublicKey(truststore, signatureOwner);

				Cipher signatureDecipher = AsymmetricCipher.startDecryptCipher(publicKey);
				String signatureInFile = AsymmetricCipher.decryptSignature(signature.getName(), signatureDecipher);

				SymmetricCipher.cipher(encryptedFile, file, symmetricDecipher);

				byte[] deciphredFileBytes = Files.readAllBytes(new File(file).toPath());
				byte[] deciphredFileBytesHash = RealDigester.digester.hash(deciphredFileBytes);
				String hashInString = Base64.getEncoder().encodeToString(deciphredFileBytesHash);

				if(!hashInString.equals(signatureInFile)) {
					sb.append("Ficheiro " + file + " foi corrompido - download não se sucedeu");
					new File(file).delete();
				}

		        encryptedFile.delete();
				//signature.delete(); Linha de código removida para efeitos de perservação do ficheiro de assinatura
				new File("./client_tools/" + workspace + ".key." + user).delete();
			}
		}
        return sb.toString();
    }
    
    /**
     * Method that sends a request to deletes files from the workspace
     * 
     * @param workspace workspace which the files will be removed from
     * @param files files to be removed
     * @return NOPERM if the user is not a member of the workspace,
     * 		   NOWS if the workspace does not exist.
     * 
     * If the current user has permission and the workspace exists,
     * for every file:
     * 		   APAGADO if the file was removed,
     * 		   Não existe no workspace indicado if the file does not exist
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public String removeFiles(String workspace, String files) throws ClassNotFoundException, IOException {
        String response = null;
        StringBuilder sb = new StringBuilder();
		String[] singularFiles = files.split(",");
		sendToServer("RM " +  workspace + " " + files);
		response = receiveMessage();
		if (!response.equals("OK"))
			return response;

		for (int i = 0; i < singularFiles.length; i++) {
			response = receiveMessage();
			sb.append(response);
			sb.append("\n          ");
		}
        return sb.toString();
    }
    
    /**
     * Method that sends a request to list the workspaces
     * that the current user is associated with
     * 
     * @return the list of the workspaces
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public String listUserWorkspaces() throws ClassNotFoundException, IOException {
    	sendToServer("LW ");
        return receiveMessage();   	
    }
    
    /**
     * Method that sends a request to list the files
     * that the workspace contains
     * 
     * @param workspace workspace to be listed
     * @return the list with the files
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public String listFilesInWorkspace(String workspace) throws ClassNotFoundException, IOException {
		sendToServer("LS " +  workspace);
        return receiveMessage(); 	
    }
    
    /**
     * Method that converts an array into a string
     * 
     * @param array array to be converted
     * @return the converted array
     */
	private String arrayToString(String[] array) {
    	StringBuilder string = new StringBuilder();
    	for (int i = 0; i < array.length; i++) {
        	if (!new File(array[i]).exists()) {
        		string.append("/");
        		string.append(array[i]);
        		string.append(",");
        	}
        	else {
        		string.append(array[i]);
    			string.append(",");
        	}
        }
    	return string.toString();
    }
	
	/**
	 * Auxiliary method used to delete the temporary files used in the 
	 * upload and download actions
	 * 
	 * @param file1
	 * @param file2
	 * @param file3
	 */
	private void deleteTemporaryFiles(String file1, String file2, String file3) {
		new File(file1).delete();
		new File(file2).delete();
		new File(file3).delete();
	}

}