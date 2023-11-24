# Java Console Chat Application
<br>

## Client.java
* The client is responsible for connecting to the server.
* You can log is using your username and password or register a new account.
* Includes the ability to send messages and receive messages from the server.
* It also includes the ability to gracefully terminate a connection when the network connection is lost.

## ClientHandler.java
* The server side handles the connection for each client.
* It receives messages from clients and broadcasts them to other clients.
* Detect and handle when a client closes a connection.

## Server.java
* Opens a server socket and waits for a connection request from the client.
* When a new client connects, we create a for that client ClientHandler and run it in a separate thread.

---
MySQL version 8.1.0
JDK 17
