# Execution of the .jar files:

MySharingServer: `java -jar MySharingServer.jar <port>`
Note: `<port>` is an optional argument. In case no `<port>` is provided, 12345 will be the default choice.
      `<port>` must be between ]1024,49151[

MySharingClient: `java -jar MySharingClient.jar <server-address>:<port> <username> <password>`
Note: `<port>` is optional, the client runs with just the IP address.

# Usernames and passwords to be used by the existing keystores
The project doesn't support a dynamic creation of keystores for each new user. As such, in order to run the project, the already existing users must be used:

<table>
  <thead>
    <tr>
      <th><code>&lt;user&gt;</code></th>
      <th><code>&lt;password&gt;</code></th>
    </tr>
  </thead>
  <tbody>
    <tr><td>salimatu</td><td>burguerqueen</td></tr>
    <tr><td>henrique</td><td>algodao</td></tr>
    <tr><td>pedro</td><td>tulipa</td></tr>
  </tbody>
</table>

# Compilation and execution of the source code:

Compilation of MySharingServer.java: 
- `cd codigo_fonte/`
- `javac MySharingServer.java`

Execution of MySharingServer: `java MySharingServer <port>`
Note: `<port>` is an optional argument. In case no `<port>` is provided, 12345 will be the default choice.
      `<port>` must be between ]1024,49151[

Compilation of MySharingClient.java: 
- `cd codigo_fonte/`
- `javac MySharingClient.java`

Execution of MySharingClient: `java MySharingClient <server-address>:<port> <username> <password>`
Note: `<port>` is optional, the client runs with just the IP address.

# Limitations of the project:
- When terminating the client with "CTRL + C" an exception of type "NoSuchElementException: no line found" is thrown, even though "CTRL + C" worked as intended. This happens due to the fact that the command line Scanner is always looking for the next input of the user. 
- The users cannot create workspaces whose name contains the string "workspace". Whenever a new user is registered, a default workspace with name "workspace" + user is created. In order to avoid conflicts with the naming of workspaces, this decision was made.
- The passwords of the keystores are the same as those used by each user to authenticate. As such, if a user decides to insert a different password, an exception is thrown. 

# Note
- The directories `sec_addons` and `client_tools` contain the keystores used by the .jar executables.
- After the first execution of MySharingServer.jar or MySharingServer.class, a new directory `server_files` will be created. It stores a file with a catalog of each username to its password, a catalog of each workspace, etc.
- Some .jpg files were provided with the intent of testing the project, and showing that these files are encrypted when transfered to the MySharingServer.
- When executing the server for the first time, a choide appears, of wether to generate a MAC or not. In case the server administrator decides not to, the server will still run normally, although without verifying the integrity of its files.
