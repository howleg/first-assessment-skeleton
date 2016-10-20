package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.ClientManager;
import com.cooksys.assessment.model.ClientSpec;
import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	// public static HashMap<String, Socket> clientHM = new HashMap<>();

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				String command = message.getCommand();

//				if (command.charAt(0) == '@') {
//					log.info("user <{}> wants to send a message to <{}> ", message.getUsername(), command.substring(1));
//					ClientManager.sendMsgToUser(command, message);
//				}

				switch (command) {
				case "connect":
					log.info("user <{}> connected", message.getUsername());
					ClientManager.addClient(new ClientSpec(message.getUsername(), socket));
					ClientManager.alert(message,true);
					break;
				case "disconnect":
					log.info("user <{}> disconnected", message.getUsername());
					ClientManager.removeClient(message.getUsername());
					ClientManager.alert(message,false);
					this.socket.close();
					break;
				case "echo":
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					//ClientManager.sendMessage(message, new ClientSpec(message.getUsername(), this.socket));
					
					ClientManager.echo(message,socket);
					
					break;
				case "users":
					log.info("user <{}> wants list of currently connected users", message.getUsername());
					ClientManager.listUsers(message, this.socket);
					break;

				case "broadcast":
					log.info("user <{}> is broadcasting <{}> to all connected users", message.getUsername(),
							message.getContents());

					ClientManager.broadcastToAll(message);
					break;

				}// end switch
				
				if (command.charAt(0) == '@') {
					log.info("user <{}> wants to send a message to <{}> ", message.getUsername(), command.substring(1));
					ClientManager.sendMsgToUser(command, message);
				}

			} // end while

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
