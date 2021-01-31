package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;

public class CommunicationHandler extends Thread {

	private Socket clientSocket;
	private ClientManager clientManager;
	private IOHandler ioHandler;
	
	public CommunicationHandler(Socket clientSocket, ClientManager clientManager) {
		this.clientSocket = clientSocket;
		this.clientManager = clientManager;
		Thread.currentThread().setName("CH@"+getIP());
		try {
			this.ioHandler = new IOHandler(clientSocket.getInputStream(), 
										   clientSocket.getOutputStream(),
										   this::processReceivedData);
		} catch (IOException e) {
			//TODO add listener
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			this.ioHandler.close();
			this.clientSocket.close();
		} catch(Exception e) {
			//TODO add Listener
			e.printStackTrace();
		}
	}
	
	protected void processReceivedData(String data) {
		this.clientManager.getServer().processReceivedData(getIP(), data);
	}
	
	public IOHandler getIOHandler() {
		return this.ioHandler;
	}
	
	public String getIP() {
		return this.clientSocket.getRemoteSocketAddress().toString();
	}
	
}
