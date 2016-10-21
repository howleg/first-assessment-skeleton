package com.cooksys.assessment.model;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientManager {

	private static List<ClientSpec> clientSpecs = Collections.synchronizedList(new ArrayList<>());

	public synchronized static void addClient(ClientSpec clientSpec) {
		if (!clientSpecs.contains(clientSpec)) {
			clientSpecs.add(clientSpec);
		}
	}

	public synchronized static void alert(Message message, boolean connected) throws IOException {
		String timeStamp = getTimeStamp();
		String msg = "";
		// ${timestamp}: <${username}> has connected
		// ${timestamp}: <${username}> has disconnected`
		if (connected)
			msg = String.format("{%s}: <%s> has connected", timeStamp, message.getUsername());
		else
			msg = String.format("{%s}: <%s> has disconnected", timeStamp, message.getUsername());

		message.setContents(msg);

		for (ClientSpec x : clientSpecs) {
			sendMessage(message, x);
		}
	}

	public synchronized static void removeClient(String username) {

		// Dedicated to Michael *bows down
		Iterator<ClientSpec> iterator = clientSpecs.iterator(); // Look more
																// closely how
																// this works.
																// #magic

		while (iterator.hasNext()) {
			ClientSpec x = iterator.next();
			if (username.equals(x.getName()))
				iterator.remove();
		}

		// for (ClientSpec x : clientSpecs) {
		// if (username.equals(x.getName())) {
		// clientSpecs.remove(x);
		// }
		// }
	}

	public synchronized static void listUsers(Message message, Socket socket) throws IOException {
		String timeStamp = getTimeStamp();

		// `${timestamp}: currently connected users:`
		// (repeated)
		// `<${username}>`

		String users = "";
		for (ClientSpec x : clientSpecs) {
			users += "   <" + x.getName() + ">\n";
		}
		message.setContents(String.format("{%s}: currently connected users:\n%s", timeStamp, users));

		for (ClientSpec x : clientSpecs) {
			if (x.getSocket() == socket)
				sendMessage(message, x);
		}

	}

	public static synchronized void echo(Message message, Socket socket) throws IOException {

		String timeStamp = getTimeStamp();

		// `${timestamp} <${username}> (echo): ${contents}`
		String msg = message.getContents();

		message.setContents(String.format("{%s} <%s> (echo): {%s}", timeStamp, message.getUsername(), msg));

		for (ClientSpec x : clientSpecs) {
			if (x.getSocket() == socket)
				sendMessage(message, x);
		}

	}

	public static synchronized void sendMessage(Message message, ClientSpec clientSpec) throws IOException {
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

		String timeStamp = getTimeStamp();
		String msg = String.format("%s <%s> (all): %s", timeStamp, message.getUsername(), message.getContents());

		// `${timestamp} <${username}> (all): ${contents}`

		message.setContents(msg);

		for (ClientSpec x : clientSpecs) {
			sendMessage(message, x);
		}

	}

	public static synchronized void sendMsgToUser(String cmd, Message message) throws IOException {

		String timeStamp = getTimeStamp();
		String msg = String.format("%s <%s> (whisper) %s", timeStamp, message.getUsername(), message.getContents());

		// ${timestamp} <${username}> (whisper): ${contents}

		message.setContents(msg);

		String rxUser = cmd.substring(1);

		for (ClientSpec x : clientSpecs) {

			if (x.getName().equals(rxUser)) {
				sendMessage(message, x);
			}
		}
	}

	private static synchronized String getTimeStamp() {
		long millis = System.currentTimeMillis();
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;
		String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
		// http://stackoverflow.com/questions/4142313/java-convert-milliseconds-to-time-format

		return time;
	}

}// end of class
