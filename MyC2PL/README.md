# Centralized Two Phase Locking Usage
Yufan Chen (chen4076@purdue.edu)

## Preparation

1. Compile java source files
```bash
javac -d bin src/centralsite/* src/distsite/* src/elements/*  src/utils/* 
```

2. Generate random transaction files with simple *select* and *update* statements
```
java -classpath "bin:lib/sqlite-jdbc-3.36.0.3.jar" utils.TransFileGenerate [transaction_num] [transaction_statement_num] [file_name]
```
For example
```bash
java -classpath "bin:lib/sqlite-jdbc-3.36.0.3.jar" utils.TransFileGenerate 40 5 transactions_40_5_1.txt
```
will generate a file with 40 transactions and each has 5 random select or update statements, and the file will be stored in *./transactions* directory as *transactions_40_5_1.txt*

## 1. Run registry at central site
```bash
# from working directory
cd bin; rmiregistry [port] &
```

## 2. Run central site
```bash
java -classpath "bin:lib/sqlite-jdbc-3.36.0.3.jar" -Djava.rmi.server.codebase=file:bin/ -Djava.security.policy=file:policy/AllPermission.policy centralsite/CentralSite [port] [deadlock_detection_period]
```
For example
```bash
java -classpath "bin:lib/sqlite-jdbc-3.36.0.3.jar" -Djava.rmi.server.codebase=file:bin/ -Djava.security.policy=file:policy/AllPermission.policy centralsite/CentralSite 1099 1000
```
will start the central site and bind its stub at registry export at port 1099, and check deadlock every 1000 milliseconds.

## 3. Run distributed sites
```bash
java -classpath "bin:lib/sqlite-jdbc-3.36.0.3.jar" -Djava.rmi.server.codebase=file:bin/ -Djava.security.policy=file:policy/AllPermission.policy distsite/DistSite [DB_NAME] [host address|localhost] [port] [transactions_file]
```
For example 
```bash
java -classpath "bin:lib/sqlite-jdbc-3.36.0.3.jar" -Djava.rmi.server.codebase=file:bin/ -Djava.security.policy=file:policy/AllPermission.policy distsite/DistSite cs542_c2pl1 localhost 1099 transactions_40_5_1.txt
```
will run a distributed site connecting the database cs542_c2pl1, which knows that the centralsite is localhost, and the registry is open at 1099 port. The site will then process the transactions in transactions_40_5_1.txt file. 