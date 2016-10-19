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

				switch (message.getCommand()) {
				case "connect":
					log.info("user <{}> connected", message.getUsername());

					// add client username to hashset
					// clientHM.put(message.getUsername(), socket);

					ClientManager.addClient(new ClientSpec(message.getUsername(), socket));

					break;
				case "disconnect":
					log.info("user <{}> disconnected", message.getUsername());

					// deletes client from hashset
					// clientHM.remove(message.getUsername());

					ClientManager.removeClient(new ClientSpec(message.getUsername(), socket));

					this.socket.close();
					break;
				case "echo":
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					String response = mapper.writeValueAsString(message);
					writer.write(response);
					writer.flush();
					break;
				case "users":
					log.info("user <{}> wants list of currently connected users", message.getUsername());
					// String clientUsernames = "";
					// for(String clientName : clientHS ){
					// clientUsernames += clientName;
					// }
					// message.setContents(clientUsernames);
					//// users:
					//// `${timestamp}: currently connected users:`
					//// (repeated)
					//// `<${username}>`
					////
					//// need to maker format client usernames like ^
					//
					String users = ClientManager.listUsers();
					message.setContents(users);
					ClientManager.sendMessage(message, new ClientSpec(message.getUsername(), this.socket));

					break;

				case "broadcast":
					log.info("user <{}> is broadcasting <{}> to all connected users", message.getUsername(),
							message.getContents());

					ClientManager.broadcastToAll(message);
					break;

				}// end switch

			} // end while

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
