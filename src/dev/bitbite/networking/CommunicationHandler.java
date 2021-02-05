package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import dev.bitbite.networking.Server.EventType;

/**
 * Manages the Communication with a client by handling its IO.
 * Runs in its own Thread and names it with the associated remote socket address (CommunicationHandler@<socket adress>).
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
			this.clientManager.getServer().notifyListeners(Server.EventType.COMMUNICATIONHANDLER_INIT_FAILED, e);
		}
	}
	
	/**
	 * Closes the IOStreams and the socket itself.
	 * 
	 * @version 0.0.1-alpha
	 */
	public void close() {
		this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE);
		try {
			this.ioHandler.close();
			this.clientSocket.close();
		} catch(Exception e) {
			this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE_FAILED, e);
			e.printStackTrace();
		}
		this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE_END);
	}
	
	/**
	 * Gets called by the IOHandler when data is received from the client
	 * and forwards the data to the server.
	 * 
	 * @param data received from the client
	 */
	protected void processReceivedData(String data) {
		this.clientManager.getServer().processReceivedData(getIP(), data);
	}
	
	/**
	 * Registers a listener to the underlying IOHandler
	 * @param listener to add
	 */
	public void registerListener(IOHandlerListener listener) {
		this.ioHandler.registerListener(listener);
	}
	
	/**
	 * Registers a list of listeners to the underlying IOHandler
	 * @param listeners to add
	 */
	public void registerListener(ArrayList<IOHandlerListener> listener) {
		listener.forEach(l -> this.ioHandler.registerListener(l));
	}
	
	/**
	 * @return the IOHandler associated with the CommunicationHandler
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
