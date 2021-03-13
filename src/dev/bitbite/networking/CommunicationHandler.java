package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import dev.bitbite.networking.Server.EventType;

/**
 * Manages the Communication with a client by handling its IO.
 * Runs in its own Thread and names it with the associated remote socket address (CommunicationHandler@[socket adress]).
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
		}
		this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE_END);
	}
	
	/**
	 * Sends data to the client
	 * @param data to send
	 */
	protected void send(String data) {
		this.ioHandler.write(data);
	}
	
	/**
	 * Gets called by the IOHandler when data is received from the client.
	 * It lets the {@link DataPreProcessor} process the data and then
	 *  forwards the data to the server.
	 * 
	 * @param data received from the client
	 */
	protected void processReceivedData(String data) {
		data = this.clientManager.getServer().getDataPreProcessor().process(DataPreProcessor.TransferMode.IN, data);
		this.clientManager.getServer().processReceivedData(this.getIP(), data);
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
	 * @param listener to add
	 */
	public void registerListener(ArrayList<IOHandlerListener> listener) {
		listener.forEach(l -> this.ioHandler.registerListener(l));
	}
	
	/**
	 * @return the remote socket address of the associated client socket
	 */
	public String getIP() {
		return this.clientSocket.getRemoteSocketAddress().toString();
	}
	
}
