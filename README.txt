- Compilação e execução do código fonte:
Compilação de MySharingServer.java: javac MySharingServer.java
Execução de MySharingServer: java MySharingServer <port>
Nota: <port> é um argumento opcional. Se não for passado um <port>, 12345 será a escolha padrão.
      <port> tem que estar entre ]1024,49151[

Compilação de MySharingClient.java: javac MySharingClient.java
Execução de MySharingClient: java MySharingClient <server-address>:<port> <username> <password>


- Execução dos ficheiros.jar:
MySharingServer: java -jar MySharingServer.jar <port>
Nota: <port> é um argumento opcional. Se não for passado um <port>, 12345 será a escolha padrão.
      <port> tem que estar entre ]1024,49151[

MySharingClient: java -jar MySharingClient.jar <server-address>:<port> <username> <password>

- Utilizadores e passwords a serem usadas pelas keystores já criadas:
<user> <password>
salimatu burguerqueen
henrique algodao
pedro tulipa

- Limitações do trabalho:
Ao terminar o cliente com "CTRL + C" é lançada uma exceção "NoSuchElementException: no line found".
Esta exceção é lançada porque o "Scanner" da linha de comandos procura sempre a próxima linha a ser inserida
pelo utilizador.
Os utilizadores não podem criar workspaces cujo nome contenha "workspace" para evitar problemas com nomes 
dos workspaces padrão criados no registo de um novo utilizador (decisão do grupo).
As passwords das keystores são iguais às que os utilizadores usam para se autenticar, logo se o utilizador inserir
uma password errada é lançada uma exceção.

- Nota
Os diretórios sec_addons e client_tools contêm as keystores necessárias para executar os ficheiros .jar.
O código fonte tem que ser compilado no formato apresentado no ficheiro .zip devido às suas dependências.
Irá ser criado um diretório "server_files" após a primeira execução de tanto MySharingServer.jar e de MySharingServer.
O grupo decidiu colocar algumas imagens para efeitos de teste do funcionamento do projeto.
Ao executar o servidor, este apresenta uma escolha de geração de ficheiros de MAC. Se o administrador decidir não gerar
estes ficheiros, a execução do servidor corre normalmente, mas sem verificações de integridade.
