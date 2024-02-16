package dev.bitbite.networking;

/**
 * This class contains all functions which, if registered at the client object, will be called
 * when certain events are happening.
 *
 * @see Client
 *
 * @version 0.0.1-alpha
 */
public abstract class ClientListener {

	/**
	 * Creates a new ClientListener
	 */
	public ClientListener() {}

	/**
	 * Will be called before the Client tries to connect to the server
	 */
	public void onConnectionCreation() {}
	/**
	 * Will be called if the connection to the server was successful
	 */
	public void onConnectionSuccess() {}
	/**
	 * Will be called if an error occured while trying to connect to the server
	 * @param e the exception which was thrown
	 */
	public void onConnectionFailed(Exception e) {}
	
	/**
	 * Will be called before the disconnection and closing processes being initiated
	 */
	public void onCloseRequested() {}
	/**
	 * Will be called after finishing the disconnection process
	 */
	public void onCloseSuccess() {}
	/**
	 * Will be called if the disconnection and closing process failed
	 * @param e the exception which was thrown
	 */
	public void onCloseFailed(Exception e) {}
	
}
