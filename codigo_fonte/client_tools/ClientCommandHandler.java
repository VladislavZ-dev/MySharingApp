package client_tools;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Class used mostly to print into the command line the status of the client's actions
 * it also makes calls to methods that perform actions in the server
 * @author Diogo Oliveira fc59839
 * @author Rodrigo Neto fc59850
 * @author Vladislav Zavgorodnii fc59783
 */
public class ClientCommandHandler {
	private String user;
	private String password;
	private ClientCommands commander;
    /**
     * Constructor. Initializes the command handler
     * @throws IOException 
     */
    public ClientCommandHandler(String user, String password) throws IOException {
    	this.user = user;
    	this.password = password;
    	this.commander = new ClientCommands(user,password);
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }

    /**
     * Method that prints the default menu of the program.
     * it displays to the user the actions he may be able to perform
     */
    public void printCommandsMenu() {
        System.out.println("Commands:\n" + //
        "CREATE <ws> <password> # Criar um novo workspace - utilizador é Owner.\n" + //
        "ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>.\n" + //
        "\tA operação ADD só funciona se o utilizador for o Owner do workspace<ws>.\n" + //
        "UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.\n" + //
        "DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.\n" + //
        "RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.\n" + //
        "LW # Lista os workspaces associados ao utilizador.\n" + //
        "LS <ws> # Lista os ficheiros dentro de um workspace.\n");
    }

    /**
     * Method that takes a command, performs the changes specified in it, and displays the status
     * in the command line
     * @param command command given by the user
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws GeneralSecurityException 
     */
    public void handleCommand(String command) throws ClassNotFoundException, IOException, GeneralSecurityException {
        
        String[] partitionedCommand = command.split(" ");
        String commandHead = partitionedCommand[0].toUpperCase();;
    	switch (commandHead) {
            case "CREATE":
            	if (partitionedCommand.length != 3) {
            		System.out.println("Comando CREATE é escrito da forma: CREATE <ws> <password>");
            		break;
            	}
                System.out.println("Resposta: " + commander.createWorkspace(partitionedCommand[1], partitionedCommand[2]));
                break;
            case "ADD":
            	if (partitionedCommand.length != 3) {
            		System.out.println("Comando ADD é escrito da forma: ADD <user1> <ws>");
            		break;
            	}
            	System.out.println("Resposta: " + commander.addMembers(partitionedCommand[1],partitionedCommand[2]));
                break;
            case "UP":
            	if (partitionedCommand.length < 3) {
            		System.out.println("Comando UP é escrito da forma: UP <ws> <file1> ... <filen>");
            		break;
            	}
            	System.out.println("Resposta: " + commander.uploadFiles(partitionedCommand[1],
            			arrayToString(Arrays.copyOfRange(partitionedCommand, 2, partitionedCommand.length))));
                break;
            case "DW":
            	if (partitionedCommand.length < 3) {
            		System.out.println("Comando DW é escrito da forma: DW <ws> <file1> ... <filen>");
            		break;
            	}
            	System.out.println("Resposta: " + commander.downloadFiles(partitionedCommand[1],
            			arrayToString(Arrays.copyOfRange(partitionedCommand, 2, partitionedCommand.length))));
                break;
            case "RM":
            	if (partitionedCommand.length < 3) {
            		System.out.println("Comando RM é escrito da forma: RM <ws> <file1> ... <filen>");
            		break;
            	}
            	System.out.println("Resposta: " + commander.removeFiles(partitionedCommand[1],
            			arrayToString(Arrays.copyOfRange(partitionedCommand, 2, partitionedCommand.length))));
                break;
            case "LW":
            	if (partitionedCommand.length != 1) {
            		System.out.println("Comando LW é escrito da forma: LW");
            		break;
            	}
            	System.out.println("Resposta: " + commander.listUserWorkspaces());
                break;
            case "LS":
            	if (partitionedCommand.length != 2) {
            		System.out.println("Comando LS é escrito da forma: LS <ws>");
            		break;
            	}
            	System.out.println("Resposta: " + commander.listFilesInWorkspace(partitionedCommand[1]));
                break;
            default:
                printCommandsMenu();
                break;
    	}
    }
    
    /**
     * Method that authenticates the user
     * 
     * @return OK if the user is authenticated, WRONG-PWD otherwise
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public String authenticate() throws ClassNotFoundException, IOException {
    	return commander.authenticate(user, password);
    }
    
    /**
     * Method that authenticates the user
     * 
     * @return OK if the user is authenticated, WRONG-PWD otherwise
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public String authenticateAgain() throws ClassNotFoundException, IOException {
    	return commander.authenticateAgain(password);
    }
    
    /**
     * Method that converts an array into a string
     * 
     * @param array array to be converted
     * @return the converted array
     */
    private String arrayToString(String[] array) {
    	StringBuilder sb = new StringBuilder();
    	for(String s : array) {
    		sb.append(s);
    		sb.append(",");  		
    	}
    	return sb.toString();
    }
}
