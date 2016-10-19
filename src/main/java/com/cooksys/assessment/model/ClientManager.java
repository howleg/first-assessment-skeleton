package com.cooksys.assessment.model;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientManager {

	private static List<ClientSpec> clientSpecs = Collections.synchronizedList(new ArrayList<>());

	public synchronized static void addClient(ClientSpec clientSpec) {
		if (!clientSpecs.contains(clientSpec)) {
			clientSpecs.add(clientSpec);
		}
	}

	public synchronized static void removeClient(ClientSpec clientSpec) {
		if (clientSpecs.contains(clientSpec)) {
			clientSpecs.remove(clientSpec);
		}
	}

	public synchronized static String listUsers() {
		String users = "";
		for (ClientSpec cs : clientSpecs) {
			users = users + cs.getName();
		}
		return users;
	}

	public static void sendMessage(Message message, ClientSpec clientSpec) throws IOException {
		/////// below holds magic. copied from original clientHandler
		/////// *** take time to understand
		/////// Probably should create a overload because making clientSpec
		/////// objects for no real reason
		Socket socket = clientSpec.getSocket();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		ObjectMapper mapper = new ObjectMapper();
		writer.write(mapper.writeValueAsString(message));
		writer.flush();
	}

	public static void broadcastToAll(Message message) throws IOException {

		for (ClientSpec x : clientSpecs) {
			sendMessage(message, x);
		}
	}

}// end of class
