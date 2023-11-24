# Java Console Chat Application
<br>

## Server.java 
* Creates a server socket and waits for a connection request from the client.
* When a new client connects, ClientHandlerwe create an object and run it in a new thread.

## ClientHandler class
* Handles each connected client.
* broadcastMessageContains methods for reading messages from a client and sending them to other clients .
* When a client leaves a chat room, there is a function to notify other clients.

## Client class
* The client side connects to the server.
* Provides membership registration and login functions. User data is stored in a database.
* Users can send messages and receive messages from other users in group chats.

---
MySQL version 8.1.0<br>
JDK 17
