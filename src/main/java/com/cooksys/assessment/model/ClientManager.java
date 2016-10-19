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

	public synchronized static void removeClient(String username) {

		for (ClientSpec x : clientSpecs) {
			if (username.equals(x.getName())) {
				clientSpecs.remove(x);
			}
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

	public static synchronized void broadcastToAll(Message message) throws IOException {
		
		
		
		String timestamp = getTimeStamp();
		String msg = String.format("%s <%s> (all): %s", timestamp, message.getUsername(), message.getContents());
		
		message.setContents(msg);
		
		
		for (ClientSpec x : clientSpecs) {
			sendMessage(message, x);		
		}
		
		
		//String s = String.format("%l <%s> (all)   ", milli, x.getName(), message.getContents())
		
		
//		`${timestamp} <${username}> (all): ${contents}`
		
	}

	public static synchronized void sendMsgToUser(String cmd, Message msg) throws IOException {

		String rxUser = cmd.substring(1);

		for (ClientSpec x : clientSpecs) {

			if (x.getName().equals(rxUser)) {
				sendMessage(msg, x);
			}
		}
	}
	
	private static String getTimeStamp()
	{
		long millis = System.currentTimeMillis();
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
		//http://stackoverflow.com/questions/4142313/java-convert-milliseconds-to-time-format
		
		return time;
	}

}// end of class
