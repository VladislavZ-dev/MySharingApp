package sec_addons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MACWriter {

    //file into which the class writes
    private File file;
    
    /**
     * Constructor. Initializes the class   
     * @param file the file to be modified
     * @throws NoSuchAlgorithmException
     */
    public MACWriter(File file) throws NoSuchAlgorithmException {
        this.file = file;
    }

    public void writeMAC(String MAC) throws IOException {
        FileWriter writer = openWriter();
        writer.write(MAC);
        closeWriter(writer);
    }

    /**
     * Auxiliary method that starts the FileWriter, ready to be used
     * @return the FileWriter
     * @throws IOException 
     */
    private FileWriter openWriter() throws IOException {
        FileWriter writer = null;
        writer = new FileWriter(file);
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
    
}
