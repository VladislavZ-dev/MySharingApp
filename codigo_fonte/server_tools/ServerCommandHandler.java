package server_tools;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import file_communication.FileTransferer;
import sec_addons.*;

/**
 * Handler responsible for the requests received by the server
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class ServerCommandHandler extends Thread{

	//paths that lead to the files used by the server
	private final String SERVER_PATH = "./server_files/";
	private final String SERVER_USERS_PATH = "./server_files/server_users.txt";
	private final String SERVER_WORKSPACES_PATH = "./server_files/server_workspaces.txt";

	//a writer and a reader for each of those files
    private FileModifier usersFileModifier;
    private FileScanner usersFileReader;
    private FileModifier workspacesFileModifier;
    private FileScanner workspacesFileReader;

	//gateways for the server's communication with the clients
    private ObjectOutputStream output;
    private ObjectInputStream input;
    
    //Synchronization locks
	private Object usersLock;
	private Object workspacesLock;
	private Object fileLock;
	
	//Current logged user
	private String loggedUser; 

	//responsible for all MAC related tasks
	private MACMaster usersMacMaster;
	private MACMaster workspacesMacMaster;

	//true if the server runs with MAC, false if not
	private boolean usesMac;
	
	/**
	 * Constructor. Initializes the handler.
	 * 
	 * @param input input stream to receive requests
	 * @param output output stream to send messages
	 * @param usersLock synchronization lock for the 'server_users.txt' file
	 * @param workspacesLock synchronization lock for the 'server_workspaces.txt' file
	 * @param fileLock synchronization lock for files in workspaces
	 * @throws NoSuchAlgorithmException
	 */
    public ServerCommandHandler(ObjectInputStream input, ObjectOutputStream output, Object usersLock, Object workspacesLock, Object fileLock,
	MACMaster usersMacMaster, MACMaster workspacesMacMaster, boolean usesMac) throws NoSuchAlgorithmException {
		usersFileModifier = new FileModifier(new File(SERVER_USERS_PATH));
    	usersFileReader = new FileScanner(new File (SERVER_USERS_PATH));
    	workspacesFileModifier = new FileModifier(new File(SERVER_WORKSPACES_PATH));
    	workspacesFileReader = new FileScanner(new File(SERVER_WORKSPACES_PATH));

    	this.output = output;
    	this.input = input;
    	this.usersLock = usersLock;
    	this.workspacesLock = workspacesLock;
    	this.fileLock = fileLock;

		this.usersMacMaster = usersMacMaster;
		this.workspacesMacMaster = workspacesMacMaster;
		this.usesMac = usesMac;
    }
    
    /**
     * Method that receives the action and processes the request
     * 
     * @param action action to be received
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchAlgorithmException 
     * @throws NoSuchPaddingException 
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws InvalidKeySpecException 
     * @throws InvalidKeyException 
     * @throws UnrecoverableKeyException 
     */
	public void executeAction(String action) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeyException,
	 InvalidKeySpecException, KeyStoreException, CertificateException, NoSuchPaddingException, UnrecoverableKeyException {
		String[] partitionedAction = action.split(" ");
        String actionHead = partitionedAction[0];

    	switch (actionHead) {
            case "CREATE":
        		insertWorkspace(partitionedAction[1], partitionedAction[2]);
        		break;
            case "ADD":
            	addUserIntoWorkspace(partitionedAction[1], partitionedAction[2]);
            	break;
            case "UP":
            	receiveFiles(partitionedAction[1], partitionedAction[2]);
            	break;
            case "DW":
            	sendFiles(partitionedAction[1], partitionedAction[2]);
            	break;
            case "RM":
            	deleteFiles(partitionedAction[1], partitionedAction[2]);
                break;
            case "LW":
            	sendListWorkspaces();
                break;
            case "LS":
            	sendListFiles(partitionedAction[1]);
                break;
    	}
	}

	/**
	 * Method that authenticates the current logged user.
	 * 
	 * @ensures the user is logged if the credentials are correct
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeyException 
	 */
    public void authenticate() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, 
    KeyStoreException, CertificateException, InvalidKeyException, NoSuchPaddingException {
		String user = receiveFromClient();
		String password = receiveFromClient();
		if (!verifyMAC()) {
			System.exit(-1);
		} 
		String code = usersFileReader.authenticateUser(user, password);

		if (code.equals("OK-NEW-USER")) {
			this.loggedUser = user;
			synchronized(usersLock){
				usersFileModifier.writeNewUser(user, password);
				usersMacMaster.writeMACIntoFile(new File(SERVER_USERS_PATH));
			}
			String defaultWorkspacePath = SERVER_PATH + "workspace" + user;
			File newWorkspace = new File(defaultWorkspacePath);
			newWorkspace.mkdir();
			synchronized(workspacesLock) {
				generateSecureWorkspace(password, "workspace" + user, defaultWorkspacePath);
			}
			sendMessage(code);
		}
		else if(code.equals("WRONG-PWD")) {
			sendMessage(code);
			String newPassword = null;
			
			while(code.equals("WRONG-PWD")){
				newPassword = receiveFromClient();
				code = usersFileReader.authenticateUser(user, newPassword);
				sendMessage(code);
			}
		}
		else {
			this.loggedUser = user;
			sendMessage(code);
		}	
    }
    
	/**
	 * Method that sends the message to a client
	 * 
	 * @param msg message to be sent
	 */
    private void sendMessage(String msg) {
    	try{
    		output.writeObject(msg);
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Method that receives a message from the client
     * 
     * @return the message received
     */
	private String receiveFromClient() {
		String fromClient = null;
		try {
			fromClient = (String) input.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return fromClient;
	}

	/**
	 * Method that verifies the integrity of the server files before those
	 * are accessed
	 * @return true if the integrity has been verified, false if not
	 * @throws UnsupportedEncodingException
	 * @throws IllegalStateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private boolean verifyMAC() throws UnsupportedEncodingException, IllegalStateException, NoSuchAlgorithmException, IOException {
		if (usesMac) {
			if (usersMacMaster.getMACFromData(Files.readAllBytes(new File (SERVER_USERS_PATH).toPath())).equals(usersMacMaster.getExistingMAC()) &&
			workspacesMacMaster.getMACFromData(Files.readAllBytes(new File (SERVER_WORKSPACES_PATH).toPath())).equals(workspacesMacMaster.getExistingMAC())) {
				return true;
			}
			System.out.println("File integrity not verified. Terminating the system..");
			return false;
		}
		return true;
		
	}

	/**
     * Auxiliary method that creates everything necessary for a new,
	 * secure workspace. It also writes into the server files
     * 
     * @ensures the directory is created and the text files are updated
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
	private void generateSecureWorkspace(String password, String workspaceName, String workspacePath) throws NoSuchAlgorithmException, 
	InvalidKeySpecException, KeyStoreException, CertificateException, IOException, InvalidKeyException, NoSuchPaddingException {

		workspacesFileModifier.writeNewWorkspace(workspaceName, loggedUser);
		workspacesMacMaster.writeMACIntoFile(new File(SERVER_WORKSPACES_PATH));

		SecretKey workspaceKey = PasswordBaseEncryptor.encryptWithPassword(password);
		PublicKey userPublicKey = KeyStoreLoader.getPublicKey(KeyStoreLoader.loadKeyStore(KeyStoreLoader.SERVER_TRUSTSTORE_PATH,
		KeyStoreLoader.SERVER_KEYSTORE_PASSWORD), loggedUser);
		AsymmetricCipher.createWorkspaceEncryptedFile(workspacePath + "/", workspaceName, loggedUser, workspaceKey, userPublicKey);
	}
    
    /**
     * Method that creates a new workspace.
     * The 'server_workspaces.txt' file is updated and a new directory is created
     * 
     * @param workspace workspace to be created
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws NoSuchPaddingException 
     * @throws InvalidKeyException 
     */
    private void insertWorkspace(String workspace, String password) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
	KeyStoreException, CertificateException, InvalidKeyException, NoSuchPaddingException {
		File newWorkspace = new File(SERVER_PATH + workspace);
		if (workspace.contains("workspace")) {
			sendMessage("NOK: Nome não pode conter 'workspace'");
			return;
		}
		if (newWorkspace.mkdir()) {
			synchronized(workspacesLock){
				generateSecureWorkspace(password, workspace, SERVER_PATH + workspace);
			}
			sendMessage("OK");
			return;
		}
        sendMessage("NOK");
    }
    
    /**
     * Method that adds the user into the workspace
     * The 'server_workspaces.txt' file is updated
     * 
     * @param user user to be added
     * @param workspace workspace the user is being added to
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     * @throws CertificateException 
     * @throws KeyStoreException 
     * @throws UnrecoverableKeyException 
     * @throws NoSuchPaddingException 
     * @throws InvalidKeyException 
     * @throws ClassNotFoundException 
     */
    private void addUserIntoWorkspace(String user, String workspace) throws IOException, NoSuchAlgorithmException, 
    KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, ClassNotFoundException {
		if (!verifyMAC())
			System.exit(-1); 
		if (workspacesFileReader.getUserWorkspaces(user).isEmpty()) {
			sendMessage("NOUSER");
			return;
		}
		if (!verifyMAC())
			System.exit(-1); 
		String permission = workspacesFileReader.checkUserIsOwner(this.loggedUser, workspace);
		sendMessage(permission);

		if (!permission.equals("OK"))
			return;
		
		FileTransferer.sendFile(SERVER_PATH + workspace + "/" + workspace + ".key." + loggedUser, output);
		FileTransferer.receiveFile(SERVER_PATH + workspace + "/" + workspace + ".key." + user, input);
		synchronized(workspacesLock) {
            workspacesFileModifier.writeNewUserIntoWorkspace(user, workspace);
			workspacesMacMaster.writeMACIntoFile(new File(SERVER_WORKSPACES_PATH));
		}
    }
    
    /**
     * Method that receives files from a client and adds them into the workspace
     * The directory is updated with the new files
     * 
     * @param workspace workspace that receives the files
     * @param files files to be received
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchAlgorithmException 
     * @throws IllegalStateException 
     */
    private void receiveFiles(String workspace, String files) throws IOException, ClassNotFoundException, 
	IllegalStateException, NoSuchAlgorithmException {
		if (!verifyMAC()) {
			System.exit(-1); 
		}
        String permission = workspacesFileReader.checkUserInWorkspace(loggedUser, workspace);
		sendMessage(permission);
        if (!permission.equals("OK"))
            return;
        
        FileTransferer.sendFile(SERVER_PATH + workspace + "/" + workspace + ".key." + loggedUser, output);
        String[] singularFiles = files.split(",");
        for (String file : singularFiles) {
        	if (file.contains("/")) {
        		sendMessage(file.substring(1) + ": Não existe");
        		continue;
        	}
        	File receivedFile = null;
        	synchronized(fileLock) {
				if (new File(SERVER_PATH + workspace + "/" + file).exists()) {
					File signatureFile = filterSignature(file, new File(SERVER_PATH + workspace).listFiles());
					signatureFile.delete();
				}
        		receivedFile = FileTransferer.receiveFile(SERVER_PATH + workspace + "/" + file, input);
				FileTransferer.receiveFile(SERVER_PATH + workspace + "/" + file + ".signed." + loggedUser, input);
    		}
            sendMessage(receivedFile.getName() + ": OK");
        }
    }
    
    /**
     * Method that sends files from the workspace to a client
     * 
     * @param workspace workspace which the files will be sent
     * @param files files to be sent
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     * @throws IllegalStateException 
     */
	private void sendFiles(String workspace, String files) throws IOException, IllegalStateException, NoSuchAlgorithmException {
		if (!verifyMAC())
			System.exit(-1); 
        String permission = workspacesFileReader.checkUserInWorkspace(loggedUser, workspace);
        sendMessage(permission);
        if (!permission.equals("OK")) {
            return;
        }

		FileTransferer.sendFile(SERVER_PATH + workspace + "/" + workspace + ".key." + loggedUser, output);

        List<String> filesWorkspace = Arrays.asList(getFilesInWorkspace(workspace));
        String[] receivedFiles = files.split(",");
        for (String file : receivedFiles) {
            if (filesWorkspace.contains(file)) {
                synchronized(fileLock) {
                	sendMessage(file + " #ficheiro transferido");
					File signatureFile = filterSignature(file, new File(SERVER_PATH + workspace).listFiles());
					String fileName = signatureFile.getName();
					String[] partitionedName = fileName.split("\\.");
					sendMessage(partitionedName[partitionedName.length - 1]);
					FileTransferer.sendFile(SERVER_PATH + workspace + "/" + fileName, output);
                	FileTransferer.sendFile(SERVER_PATH + workspace + "/" + file, output);
            	}
            } 
            else {
                sendMessage("O ficheiro " + file + " não existe no workspace indicado");
            }
        }
    }
    
	/**
	 * Method that deletes files from the workspace
	 * The directory is updated with the missing files
	 * 
	 * @param workspace workspace which the files will be deleted
	 * @param files files to be deleted
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws IllegalStateException 
	 */
	private void deleteFiles(String workspace, String files) throws IOException, IllegalStateException, NoSuchAlgorithmException {
		if (!verifyMAC())
			System.exit(-1); 
        String permission = workspacesFileReader.checkUserInWorkspace(loggedUser, workspace);
        sendMessage(permission);
        if (!permission.equals("OK")) {
            return;
        }

        String[] fileList = files.split(",");
        for (String singularFile : fileList) {
        	String[] filesInWorkspace = getFilesInWorkspace(workspace); 
        	boolean deleted = false;
        	if (filesInWorkspace.length == 0) {
        		sendMessage("O ficheiro " + singularFile + " não existe no workspace indicado");
        		continue;
        	}
	        for (String file : filesInWorkspace) {
	            if (singularFile.equals(file)) {
	            	synchronized(fileLock) {
						deleteSignatureOfFile(workspace, file);
	                	new File(SERVER_PATH + workspace + "/" + file).delete();
	            	}
	                deleted = true;
	                sendMessage(singularFile + ": APAGADO");
	                break;
	            }
			}
	        if (!deleted)
	        	sendMessage("O ficheiro " + singularFile + " não existe no workspace indicado");
		}
    }

	/**
	 * Auxiliary method that deletes the signature of a soon 
	 * to be deleted file
	 * @param workspace the workspace where the deletion occurs
	 * @param toBeDeleted the name of the soon to be deleted file
	 */
    private void deleteSignatureOfFile(String workspace, String toBeDeleted) {
        File fileToDelete = new File(SERVER_PATH + workspace + "/" + toBeDeleted);
        File workspaceDirectory = fileToDelete.getParentFile();
		File signature = filterSignature(toBeDeleted, workspaceDirectory.listFiles());
		if (signature != null)
			signature.delete();
    }

	/**
	 * Auxiliary method responsible for searching and fetching the 
	 * signature file of a soon to be deleted file
	 * @param toBeDeleted the name of the soon to be deleted file
	 * @param candidateFiles array of files where the method searches
	 * @return the signature file needed
	 * @ensures will always find the signature if the original file exists
	 */
	private File filterSignature(String toBeDeleted, File[] candidateFiles) {
		for (File file : candidateFiles) {
			if (file.getName().contains(toBeDeleted + ".signed.")) 
				return file;
		}
		return null;
	}

    
	/**
	 * Method that sends a list containing the workspaces 
	 * the current logged user is associated with
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 * @throws IllegalStateException 
	 * @ensures the list contains at least one element
	 */
    private void sendListWorkspaces() throws IOException, IllegalStateException, NoSuchAlgorithmException {
    	
    	StringBuilder string = new StringBuilder();
    	string.append("{ ");
		if (!verifyMAC())
			System.exit(-1); 
    	Set<String> workspaceNames = workspacesFileReader.getUserWorkspaces(this.loggedUser);
    	
    	for (String name : workspaceNames) {
			string.append(name);
    		string.append(", ");
    	}
		string.deleteCharAt(string.length() -2);
    	string.append("}");
    	
    	sendMessage(string.toString());
    }
    
    /**
     * Method that sends a list containing the files 
     * in the workspace to a client
     * 
     * @param workspace workspace which the files belong to
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     * @throws IllegalStateException 
     */
    private void sendListFiles(String workspace) throws IOException, IllegalStateException, NoSuchAlgorithmException {
		if (!verifyMAC())
			System.exit(-1); 
		String permission = workspacesFileReader.checkUserInWorkspace(loggedUser, workspace);
		if (!permission.equals("OK")) {
			sendMessage(permission);
			return;
		}
    	StringBuilder string = new StringBuilder();
    	
    	String[] fileNames = getFilesInWorkspace(workspace);
    	if(fileNames.length == 0) {
    		sendMessage("{ }");
    		return;
    	}
    	string.append("{ ");
    	string.append(fileNames[0]);
    	for (int i = 1; i < fileNames.length; i++) {
    		string.append(", ");
    		string.append(fileNames[i]);
    	}
    	string.append(" }");
    	
    	sendMessage(string.toString());
    }
    
    /**
     * Method that returns a list of the files 
     * in the workspace's directory
     * 
     * @param workspace
     * @return
     */
	private String[] getFilesInWorkspace(String workspace) {
        File directory = new File(SERVER_PATH + workspace + "/");
		return directory.list();
	}
}