package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import dev.bitbite.networking.Server.EventType;
import lombok.Getter;

/**
 * Manages the Communication with a client by handling its IO.
 */
public class CommunicationHandler {

	private Socket clientSocket;
	private ClientManager clientManager;
	@Getter private IOHandler iOHandler;
	
	/**
	 * Creates a CommunicationHandler object for a socket
	 * @param clientSocket which IO should be handled
	 * @param clientManager the clientManager of the server which accepted the client
	 */
	public CommunicationHandler(Socket clientSocket, ClientManager clientManager) {
		this.clientSocket = clientSocket;
		this.clientManager = clientManager;
		try {
			this.iOHandler = new IOHandler(clientSocket.getInputStream(), 
										   clientSocket.getOutputStream(),
										   this::processReceivedData);
			this.iOHandler.registerListener(new CommunicationHandlerCloseListener(this));
		} catch (IOException e) {
			this.clientManager.getServer().notifyListeners(Server.EventType.COMMUNICATIONHANDLER_INIT_FAILED, e);
		}
	}
	
	/**
	 * Closes the IOStreams and the socket itself.
	 */
	public void close() {
		this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE, this);
		try {
			this.iOHandler.close();
			this.clientSocket.close();
			this.clientManager.removeCommunicationHandler(this);
		} catch(Exception e) {
			this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE_FAILED, this, e);
		}
		this.clientManager.getServer().notifyListeners(EventType.COMMUNICATIONHANDLER_CLOSE_END, this);
	}
	
	/**
	 * Sends data to the client
	 * @param data to send
	 */
	protected void send(byte[] data) {
		this.iOHandler.write(data);
	}
	
	/**
	 * Forces the currently read bytes to be handled
	 */
	public void flushRead() {
		this.iOHandler.flushRead();
	}
	
	/**
	 * Blocks until the given amount of bytes are read
	 * @param amount
	 */
	public void readNBytes(int amount) {
		this.iOHandler.readToNBytes(amount);
	}
	
	/**
	 * Gets called by the IOHandler when data is received from the client.
	 * It lets the {@link DataPreProcessor} process the data and then
	 *  forwards the data to the server.
	 * 
	 * @param data received from the client
	 */
	protected void processReceivedData(byte[] data) {
		data = this.clientManager.getServer().getDataPreProcessor().process(DataPreProcessor.TransferMode.IN, data);
		this.clientManager.getServer().processReceivedData(this.getIP(), data);
	}
	
	/**
	 * Registers a listener to the underlying IOHandler
	 * @param listener to add
	 */
	public void registerListener(IOHandlerListener listener) {
		this.iOHandler.registerListener(listener);
	}
	
	/**
	 * Registers a list of listeners to the underlying IOHandler
	 * @param listener to add
	 */
	public void registerListener(ArrayList<IOHandlerListener> listener) {
		listener.forEach(l -> this.iOHandler.registerListener(l));
	}

	/**
	 * @return the remote socket address of the associated client socket
	 */
	public String getIP() {
		return this.clientSocket.getRemoteSocketAddress().toString();
	}
	
}
