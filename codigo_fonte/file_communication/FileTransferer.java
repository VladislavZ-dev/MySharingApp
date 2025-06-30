package file_communication;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Class used to transfer and receive files over a socket connection
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class FileTransferer {

    //buffer size used during the file exchange
    private static final int BUFFER_SIZE = 1024;

    /**
     * Method that sends a given file through a connection
     * starts by sending the file size to the receiver, then opens an input stream.
     * and lastly sends the file
     * @param filename name of the file to be sent
     * @param output Output Stream of the connection
     * @throws IOException 
     * @ensures the receiver will receive the file without any issue
     */
    public static void sendFile(String filename, ObjectOutputStream output) throws IOException {
        File file = new File(filename);
        int size = (int) file.length();
        output.writeObject(size);

        FileInputStream fin = new FileInputStream(file);
        InputStream input = new BufferedInputStream(fin);

        byte[] buffer = null;
        int read = 0;

        while (size != 0) {
            if (size <= BUFFER_SIZE) {
                buffer = new byte[size];
                read = input.read(buffer, 0, size);
            }
            else {
                buffer = new byte[BUFFER_SIZE];
                read = input.read(buffer, 0, BUFFER_SIZE);
            }
            output.write(buffer, 0, read);
            output.flush();
            size -= read;
        }
        input.close();
        
    }

    /**
     * Method that receives a file from a connection, and stores its content into a given file
     * starts by receiving the file size from the sender, then opens an output stream.
     * and lastly receives the content of the sent file and stores it in the given file
     * @param filename name of the file where the content will be stored
     * @param input Input Stream of the connection
     * @throws ClassNotFoundException 
     * @ensures the sent file will be received without any issue
     */
    public static File receiveFile(String filename, ObjectInputStream input) throws IOException, ClassNotFoundException {
        File file = new File(filename);
    	int size;
        size = (int) input.readObject();
        file.createNewFile();
        FileOutputStream fout = new FileOutputStream(file);
        OutputStream output = new BufferedOutputStream(fout);

        byte[] buffer = null;
        int read = 0;

        while (size != 0) {
            if (size <= BUFFER_SIZE) {
                buffer = new byte[size];
                read = input.read(buffer, 0, size);
            }
            else {
                buffer = new byte[BUFFER_SIZE];
                read = input.read(buffer, 0, BUFFER_SIZE);
            }
            output.write(buffer, 0, read);
            output.flush();
            size -= read;
            }
        output.close();
        return file;
    }
}
