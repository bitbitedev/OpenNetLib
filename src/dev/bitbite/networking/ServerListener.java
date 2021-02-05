package dev.bitbite.networking;

/**
 * This class contains all functions which, if registered at the server object, will be called
 * when certain events are happening.
 *
 * @see Server
 *
 * @version 0.0.1-alpha
 */
public abstract class ServerListener {

	/**
	 * Will be called before server startup
	 */
	public void onStart() {}
	/**
	 * Will be called if server start failed
	 * @param e the exception thrown while trying to start the server
	 */
	public void onStartFailed(Exception e) {}
	/**
	 * Will be called after successful server start
	 */
	public void onStartSuccess() {}
	
	/**
	 * Will be called once a client has been accepted by the server
	 * @param ch the CommunicationHandler associated with the client that just connected
	 */
	public void onAccept(CommunicationHandler ch) {}
	/**
	 * Will be called before the server starts to accept clients
	 */
	public void onAcceptStart() {}
	/**
	 * Will be called once the server stops to accept clients
	 */
	public void onAcceptEnd() {}
	/**
	 * Will be called if an error occures while trying to accept a client
	 * @param e the exception which was thrown
	 */
	public void onAcceptFailed(Exception e) {}
	
	/**
	 * Will be called before the closing process of the server is beeing initiated
	 */
	public void onClose() {}
	/**
	 * Will be called once the server has been closed successfully
	 */
	public void onCloseEnd() {}
	/**
	 * Will be called if an error occures while trying to close the server
	 * @param e the exception which was thrown
	 */
	public void onCloseFailed(Exception e) {}
	
	/**
	 * Will be called if an error occures while trying to init the IOHandler inside the CommunicationHandler
	 * @param exception which was thrown
	 */
	public void onCommunicationHandlerInitFailed(Exception exception) {}
	
	/**
	 * Will be called before a CommunicationHandler is tried to be closed
	 */
	public void onCommunicationHandlerClose() {}
	
	/**
	 * Will be called once a CommunicationHandler is closed
	 */
	public void onCommunicationHandlerCloseEnd() {}
	
	/**
	 * Will be called if the closing process fails
	 * @param exception which was thrown during the process of closing the CommunicationHandler
	 */
	public void onCommunicationHandlerCloseFailed(Exception exception) {}
	
	/**
	 * Will be fired if the ServerSocket is closed while Client tried to connect.
	 * @param exception which is thrown during the process of client acceptance.
	 */
	public void onSocketClosed(Exception exception) {}
}
