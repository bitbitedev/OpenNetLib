package dev.bitbite.networking;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

/**
 * The SSLClient class is an abstract class that represents a client that communicates over SSL/TLS.
 * It extends the Client class and provides an implementation for opening an SSL socket.
 */
public abstract class SSLClient extends Client {

	/**
	 * Constructs a new SSLClient with the specified host and port.
	 *
	 * @param host the host to connect to
	 * @param port the port to connect to
	 */
	public SSLClient(String host, int port) {
		super(host, port);
	}

	/**
	 * Opens an SSL socket using the default SSLSocketFactory.
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs while creating the socket
	 */
	@Override
	protected void openSocket() throws UnknownHostException, IOException {
		super.socket = SSLSocketFactory.getDefault().createSocket(super.HOST, super.PORT);
	}

	/**
	 * Processes the received data.
	 *
	 * @param data the received data
	 */
	@Override
	protected abstract void processReceivedData(byte[] data);

}
