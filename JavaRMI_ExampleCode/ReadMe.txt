Run
rmiregistry

Start Server:
From the folder with binary files
specifying the option "-Djava.rmi.server.codebase"
(For example,
Linux: -Djava.rmi.server.codebase=file:/tkuznets/workspace/Server/bin/
Windows: -Djava.rmi.server.codebase=file:D:\Eclipse\workspace\Server\bin
)
run
java -Djava.rmi.server.codebase=file:D:\Eclipse\workspace\Server\bin Server

Start Client:
java -Djava.rmi.server.codebase=file:D:\Eclipse\workspace\Client\bin Client
