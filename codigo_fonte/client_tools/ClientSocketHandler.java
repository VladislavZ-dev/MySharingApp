package client_tools;
import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Handler for the client socket
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class ClientSocketHandler {
	private static SSLSocket clientSocket;
	
	/**
	 * Method that connects to the server
	 * 
	 * @param serverAddress the address of the server
	 * @throws IOException
	 */
	public static void connectToServer (String serverAddress) throws IOException {
    	String[] ipPort = serverAddress.split(":");
    	SocketFactory sf = SSLSocketFactory.getDefault();
    	if (ipPort.length == 1) {
			clientSocket = (SSLSocket) sf.createSocket(ipPort[0], 12345);
    	}
    	else {
    		clientSocket = (SSLSocket) sf.createSocket(ipPort[0], Integer.parseInt(ipPort[1]));
    	}
    }
	
	/**
	 * Method that disconnects form the server
	 * 
	 * @throws IOException
	 */
    public static void disconnectFromServer() throws IOException {
    	clientSocket.close();
    }
    
    /**
     * Method that returns the client socket
     * 
     * @return the client socket
     */
    public static Socket getSocket() {
    	return clientSocket;
    }
}
