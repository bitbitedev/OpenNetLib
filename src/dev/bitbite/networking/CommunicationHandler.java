package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;

/**
 * Manages the Communication with a client by handling its IO.
 * Runs in its own Thread and names it with the associated remote socket address.
 * 
 * @version 0.0.1-alpha
 */
public class CommunicationHandler extends Thread {

	private Socket clientSocket;
	private ClientManager clientManager;
	private IOHandler ioHandler;
	
	/**
	 * Creates a CommunicationHandler object for a socket
	 * @param clientSocket which IO should be handled
	 * @param clientManager the clientManager of the server which accepted the client
	 */
	public CommunicationHandler(Socket clientSocket, ClientManager clientManager) {
		this.clientSocket = clientSocket;
		this.clientManager = clientManager;
		Thread.currentThread().setName("CommunicationHandler@"+getIP());
		try {
			this.ioHandler = new IOHandler(clientSocket.getInputStream(), 
										   clientSocket.getOutputStream(),
										   this::processReceivedData);
		} catch (IOException e) {
			//TODO add listener
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the IOStreams and the socket itself.
	 * 
	 * @version 0.0.1-alpha
	 */
	public void close() {
		try {
			this.ioHandler.close();
			this.clientSocket.close();
		} catch(Exception e) {
			//TODO add Listener
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets called by the IOHandler when data is received from the client
	 * and propagates the data to the server.
	 * 
	 * @param data received from the client
	 */
	protected void processReceivedData(String data) {
		this.clientManager.getServer().processReceivedData(getIP(), data);
	}
	
	/**
	 * @return the IOHandler associates with thie CommunicationHandler
	 */
	public IOHandler getIOHandler() {
		return this.ioHandler;
	}
	
	/**
	 * @return the remote socket address of the associated client socket
	 */
	public String getIP() {
		return this.clientSocket.getRemoteSocketAddress().toString();
	}
	
}
