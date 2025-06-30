package server_tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import sec_addons.*;

/**
 * Class used to write into a text file
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class FileModifier {

    //file into which the class writes
    private File file;
    
    /**
     * Constructor. Initializes the class   
     * @param file the file to be modified
     * @throws NoSuchAlgorithmException
     */
    public FileModifier(File file) throws NoSuchAlgorithmException {
        this.file = file;
    }

    /**
     * Auxiliary method that starts the FileWriter, ready to be used
     * @return the FileWriter
     * @throws IOException 
     */
    private FileWriter openWriter() throws IOException {
        FileWriter writer = null;
        writer = new FileWriter(file, true);
        return writer;
    }

    /**
     * Auxiliary method that closes a given FileWriter
     * @param writer FileWriter to be closed
     * @throws IOException 
     */
    private void closeWriter(FileWriter writer) throws IOException {
    	writer.close();
    }
    /**
     * Method that writes a new user into the file 
     * @param user User to be written
     * @throws IOException 
     */
    public void writeNewUser(String user, String password) throws IOException {
        FileWriter writer = openWriter();
        byte[] salt = SaltMaster.giftSalt();
        String hashedAndSaltedPassword = RealDigester.hashPasswordWithSalt(password, salt);
        String saltString = RealDigester.encodeSaltIntoString(salt);
        writer.write(user + ":" + hashedAndSaltedPassword + ":" + saltString + "\n");
        closeWriter(writer);
    }

    /**
     * Method that writes a new workspace into the file
     * @param workspace Workspace to be written
     * @throws IOException 
     */
    public void writeNewWorkspace(String workspace, String owner) throws IOException {
        FileWriter writer = openWriter();
        writer.write(workspace + ":" + owner + ":" + owner + "\n");
        closeWriter(writer);
    }
    
    /**
     * Method that adds a new user into the workspace
     * 
     * @param userToAdd the user to be added
     * @param workspace workspace which the user will be added
     * @throws IOException
     */
    public void writeNewUserIntoWorkspace(String userToAdd, String workspace) throws IOException  {
        BufferedReader fileReader = new BufferedReader(new FileReader(this.file));
        StringBuffer inputBuffer = new StringBuffer();
        String toChange;

        while ((toChange = fileReader.readLine()) != null) {
            String[] lineToChange = toChange.split(":");
            if (lineToChange[0].equals(workspace) && !lineToChange[2].contains(userToAdd)) {
                toChange =  toChange + "," + userToAdd;
                inputBuffer.append(toChange);
                inputBuffer.append('\n');
            }
            else {
                inputBuffer.append(toChange);
                inputBuffer.append('\n');
            }
            
        }
        fileReader.close();

        FileOutputStream fileOut = new FileOutputStream(this.file);
        fileOut.write(inputBuffer.toString().getBytes());
        fileOut.close();
    }
}



