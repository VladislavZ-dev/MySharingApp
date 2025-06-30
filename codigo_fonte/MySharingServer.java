import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import sec_addons.MACMaster;
import sec_addons.PasswordBaseEncryptor;
import server_tools.ServerCommandHandler;

/**
 * Class that executes the server and creates the files
 * necessary to its execution
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class MySharingServer {
	
	//Synchronization locks
	private static final Object USERS_LOCK = new Object();
	private static final Object WORKSPACES_LOCK = new Object();
	private static final Object FILE_LOCK = new Object();

	//files directly used by the server
	static File usersFile = new File("./server_files/server_users.txt");
	static File workspacesFile = new File("./server_files/server_workspaces.txt");
	static File usersMAC = new File("./server_files/userMAC.txt");
	static File workspacesMAC = new File("./server_files/workspaceMAC.txt");

	//server's secret key created at the start of its execution
	static SecretKey serverKey;

	//responsible for all MAC related actions throughout the execution
	static MACMaster usersMACMaster;
	static MACMaster workspacesMACMaster;

	//needed in order for the server to be able to operate without a MAC calculated
	static boolean usesMac = false;
	
	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.keyStore", "./sec_addons/keystores/keystore.server");
		System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
		System.setProperty("javax.net.ssl.trustStore", "./sec_addons/keystores/truststore.server");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
		System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");

		File server_files = new File("./server_files/");
		server_files.mkdir();
		
		try {
			usersFile.createNewFile();
			workspacesFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int port;
		if(args.length == 0) 
			startServer(12345);
		else if(args.length == 1 && isNumeric(args[0]) ){
			port = Integer.parseInt(args[0]);
			
			if(1024 < port && port < 49151)
				startServer(port);
			else 
				System.out.println("O argumento <port> tem que estar entre ]1024,49151[");
		}
		else 
			System.out.println("O programa é corrido da forma:\n\tmySharingServer <port>");
	}
	
	/**
	 * Method that checks if a string has
	 * only numeric characters
	 * 
	 * @param str the string to be checked
	 * @return true if it only contains numeric characters,
	 * 		   false if not
	 */
	private static boolean isNumeric(String str) {
		for(int i = 0; i < str.length(); i++) {
			if(!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Method responsible to start the server and create
	 * threads for the clients
	 * 
	 * @param port port that the server will start with
	 */
	public static void startServer (int port){
	
		SSLServerSocket sSoc = null;
		try {
			try {
				String permission = verifyIntegrity();
				if (permission.equals("TERMINATE"))
					return;
				else if (permission.equals("MAC"))
					usesMac = true;
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
			
			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
			sSoc = (SSLServerSocket) ssf.createServerSocket(port);
			sSoc.setNeedClientAuth(true);

			System.out.println("Servidor iniciado!");
			while(true) {
				try {
					Socket inSoc = sSoc.accept();
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
				}
				
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		finally {
			try {
				if (sSoc != null)
					sSoc.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method that verifies the integrity of the server files at the start 
	 * of the server's execution
	 * @return "MAC" if the integrity has been verified
	 * 			"NOMAC" if the server's administrator has chosen to not
	 * 			calculate the MAC
	 * 			"TERMINATE" if the integrity has not been verified, which forces
	 * 			the server to shutdown
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	public static String verifyIntegrity() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Por favor insira a palavra passe do sistema");
		String password = scanner.nextLine();

		serverKey = PasswordBaseEncryptor.encryptMac(password);

		if (!usersMAC.exists() && !workspacesMAC.exists()) {

			System.out.println("Parece que não existe nenhum MAC. Gostaria de gerar um? (s/n)");
			if (scanner.nextLine().toUpperCase().equals("S")) {

				usersMAC.createNewFile();
				workspacesMAC.createNewFile();

				usersMACMaster = new MACMaster(usersMAC, true);
				usersMACMaster.setKey(serverKey);
				usersMACMaster.writeMACIntoFile(usersFile);

				workspacesMACMaster = new MACMaster(workspacesMAC, true);
				workspacesMACMaster.setKey(serverKey);
				workspacesMACMaster.writeMACIntoFile(workspacesFile);

				scanner.close();
				return "MAC";
			} else {

				usersMACMaster = new MACMaster(usersMAC, false);
				workspacesMACMaster = new MACMaster(workspacesMAC, false);

				scanner.close();
				return "NOMAC";
			}
		}
		else {
			usersMACMaster = new MACMaster(usersMAC, true);
			workspacesMACMaster = new MACMaster(workspacesMAC, true);
			usersMACMaster.setKey(serverKey);
			workspacesMACMaster.setKey(serverKey);
			if (usersMACMaster.getMACFromData(Files.readAllBytes(usersFile.toPath())).equals(usersMACMaster.getExistingMAC())) {
				scanner.close();
				return "MAC";
			}
			scanner.close();
			return "TERMINATE";
		}
	}

	/**
	 * Class responsible to create threads for the clients
	 */
	static class ServerThread extends Thread {

		private Socket socket = null;
		
		/**
		 * Constructor. Initializes the server socket
		 * 
		 * @param inSoc server socket
		 */
		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("Ligação com cliente iniciada!");
		}
		
		/**
		 * Method that runs the thread
		 */
		public void run() {
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			ServerCommandHandler handler = null;
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				handler = new ServerCommandHandler(in,out,USERS_LOCK,WORKSPACES_LOCK, FILE_LOCK, usersMACMaster, workspacesMACMaster, usesMac);
				handler.authenticate();
			} catch (IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException 
					| KeyStoreException | CertificateException | NoSuchPaddingException e) {
				e.printStackTrace();			
			}
			
			while(true) {
				String action = null;
				try {
					action = (String) in.readObject();
					handler.executeAction(action);
				}
				catch (SocketException | UnrecoverableKeyException |EOFException e) {
					System.out.println("Conexão com o cliente terminada!");
					break;
				}
				catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException | 
				KeyStoreException | CertificateException | NoSuchPaddingException e) {
					e.printStackTrace();
				}
			}
		}
	}
}