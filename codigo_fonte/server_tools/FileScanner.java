package server_tools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import sec_addons.*;

/**
 * Class used to read a text file
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class FileScanner {

    //the file to be read
    private File file;


    /**
     * Constructor. Initializes the FileReader with a file to be read
     * @param file the file to be read
     * @throws NoSuchAlgorithmException
     */
    public FileScanner(File file) throws NoSuchAlgorithmException {
        this.file = file;
    }

    /**
     * Method that reads and returns just the first line of the file
     * @return the first line of the file
     * @throws IOException
     */
    public String readFirstLine() throws IOException {
        Scanner reader = createReader();
        String firstLine = reader.nextLine();
        closeReader(reader);
        return firstLine;
    }

    /**
     * Auxiliary method that starts the FileReader, ready to be used
     * @return the FileReader
     * @throws FileNotFoundException 
     */
    protected Scanner createReader() throws IOException {
        Scanner scanner = null;
        scanner = new Scanner(this.file);
        return scanner;
    }

    /**
     * Auxiliary method that closes a given FileReader
     * @param reader FileReader to be closed
     */
    protected void closeReader(Scanner reader) {
        reader.close();
    }

    /**
     * Method that reads the file, and verifies if a given user was written into the file
     * @param user the user to be verified
     * @return the password if the user exists, null if it not
     * @throws FileNotFoundException 
     */
    private String checkUserExists(String user) throws IOException {
        Scanner reader = createReader();
        while(reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] userInfo = line.split(":");
            if (user.equals(userInfo[0])) {
                closeReader(reader);
                return (userInfo[1]+":"+userInfo[2]);
            }
        }
        closeReader(reader);
        return null;
}
    /**
     * Method that reads the file, and tries to authenticate the user, by comparing its password to the
     * one present on the file
     * @param user the user to be authenticated
     * @assumes user exists on the file
     * @return true if the user was successfully authenticated, false if not
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public String authenticateUser(String user, String password) throws IOException {
    	String userPassword = checkUserExists(user); 
    	if (userPassword == null) {
    		return "OK-NEW-USER";
    	}
        String[] passInFile = userPassword.split(":");
        String hashedPassword = RealDigester.hashPasswordWithSalt(password, passInFile[1]);
        if (hashedPassword.equals(passInFile[0])) {
    	   return "OK-USER";
        }
        return "WRONG-PWD";
    }
    
    /**
     * Method that reads the file and returns the workspaces of the user
     * 
     * @param user the user to get the workspaces
     * @return a set with the user workspaces
     * @throws IOException
     */
    public Set<String> getUserWorkspaces(String user) throws IOException {
    	Scanner scanner = createReader();
    	Set<String> workspaceList = new HashSet<>();
    	
    	while(scanner.hasNextLine()) {
    		String[] partitionedLine = scanner.nextLine().split(":");
    		String[] members = partitionedLine[2].split(",");
    		for (String member : members) {
    			if (member.equals(user)) {
    				workspaceList.add(partitionedLine[0]);
    				break;
    			}
    		}
    	}
    	closeReader(scanner);
    	return workspaceList;
    }
    
    /**
     * Method that checks if the user is the owner of the workspace
     * 
     * @param user the user to check
     * @param workspace workspace whose owner we want to get
     * @return NOWS if workspace does not exist. 
     * 		   OK if user is owner, NOPERM if it is not. 
     * @throws IOException
     */
    protected String checkUserIsOwner(String user, String workspace) throws IOException {
        Scanner sc = createReader();

        while (sc.hasNextLine()) {
            String[] workspaceInfo = sc.nextLine().split(":");
            if (workspaceInfo[0].equals(workspace) && workspaceInfo[1].equals(user)) {
            	closeReader(sc);
                return "OK";
            }
            else if (workspaceInfo[0].equals(workspace) && !workspaceInfo[1].equals(user)) {
            	closeReader(sc);
                return "NOPERM";
            }
        }
        closeReader(sc);
        return "NOWS";
    }
    
    /**
     * Method that checks if the user is a member of the workspace
     * 
     * @param user the user to be checked
     * @param workspace workspace whose members we want to check
     * @return NOWS if workspace does not exist. 
     * 		   OK if user is member, NOPERM if it is not. 
     * @throws IOException
     */
    protected String checkUserInWorkspace(String user, String workspace) throws IOException {
    	Scanner sc = createReader();

        while (sc.hasNextLine()) {
            String[] workspaceInfo = sc.nextLine().split(":");
            if (workspaceInfo[0].equals(workspace) && workspaceInfo[2].contains(user)) {
            	closeReader(sc);
                return "OK";
            }
            else if (workspaceInfo[0].equals(workspace) && !workspaceInfo[2].contains(user)) {
            	closeReader(sc);
                return "NOPERM";
            }
        }
        closeReader(sc);
        return "NOWS";
    }
}
