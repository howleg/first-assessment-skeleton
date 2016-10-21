package com.cooksys.assessment.model;

import java.net.Socket;

public class ClientSpec {
	private Socket socket;
	private String name;

	public ClientSpec(String name, Socket socket) {
		this.name = name;
		this.socket = socket;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
