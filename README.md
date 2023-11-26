# Java Console Chat Application
<br>

## Server.java 
* Sets up a server socket and waits for client connections.
* On a new connection, it creates a new ClientHandler thread for each client.
* Contains methods to start the server and close the server socket.

## ClientHandler class
* Handles client communication, including reading and sending messages to the client.
* Broadcasts messages from one client to all other clients.
* Manages chat history loading and deletion.
* Closes connections and manages client disconnection.

## Client class
* Connects to the server socket.
* Handles user registration and login using a MySQL database.
* Sends and receives messages from the server.
* Includes methods for hashing passwords and closing connections.

## MongoDBUtil class
* Connects to a MongoDB database.
* Provides methods to add, retrieve, and delete chat messages.
---
# Extends Library
* JDK-17.jdk
* mysql-connector-j-8.1.0.jar
* mongodb-jdbc-2.0.3-all.jar
* slf4j-api-2.0.3.jar
* slf4j-simple-2.0.3.jar
  * slf4j.jar install [link](https://repo1.maven.org/maven2/org/slf4j/)


