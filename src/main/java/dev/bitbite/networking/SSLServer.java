package dev.bitbite.networking;

import javax.net.ssl.SSLServerSocketFactory;

/**
 * The SSLServer class is an abstract class that represents a server that uses SSL/TLS for secure communication.
 * It extends the Server class and provides an implementation for opening a server socket using SSL.
 * Subclasses of SSLServer must implement the processReceivedData method to handle received data from clients.
 */
public abstract class SSLServer extends Server {

	/**
	 * Constructs a new SSLServer with the specified port.
	 *
	 * @param port the port number to listen on
	 */
	public SSLServer(int port) {
		super(port);
	}

	/**
	 * Opens a server socket using SSLServerSocketFactory.getDefault() and assigns it to the serverSocket field.
	 * If an exception occurs, it notifies the listeners with the START_FAILED event type and the exception.
	 */
	@Override
	protected void openServerSocket() {
		try {
			super.serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(super.PORT);
		} catch (Exception e) {
			this.notifyListeners(EventType.START_FAILED, e);
		}
	}

	/**
	 * Subclasses must implement this method to process received data from clients.
	 *
	 * @param clientAddress the address of the client
	 * @param data          the received data
	 */
	@Override
	protected abstract void processReceivedData(String clientAddress, byte[] data);

}
