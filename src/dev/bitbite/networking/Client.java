package dev.bitbite.networking;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Represents an abstract implementation of the client-side connection.<br>
 * The connection process can be initiated using the client objects {@link #connect()} method, 
 * which will return true, if the connection process has been successful.<br>
 * Closing the connection can be done using the client objects {@link #close()} method,
 * which will return true, if the disconnection process has been completed successfully.<br>
 * Incomming data from the server will be propagated to {@link #processReceivedData(String)}.<br>
 * Data to the server can be sent using the clients {@link IOHandler}.<br>
 * Some events trigger the notification of registered {@link ClientListener}s. 
 * 
 * @see ClientListener
 * 
 * @version 0.0.1-alpha
 */
public abstract class Client {

	private final String HOST;
	private final int PORT;
	private Socket socket;
	private IOHandler ioHandler;
	private boolean keepAlive = false;
	private ArrayList<ClientListener> listeners;
	
	/**
	 * The different event-types listeners can listen on
	 * 
	 * @see ClientListener
	 * @version 0.0.1-alpha
	 */
	enum EventType {
		CONNECTION,
		CONNECTION_SUCCESS,
		CONNECTION_FAILED,
		CLOSE,
		CLOSE_FAILED,
		CLOSE_SUCCESS
	}
	
	/**
	 * Creates a Client object and sets the endpoint on which on
	 * startup the client will try to connect to
	 * @param host the domain or IP-adress without protocoll information or port
	 * @param port to connect to
	 */
	public Client(String host, int port) {
		this.HOST = host;
		this.PORT = port;
	}
	
	/**
	 * Initiates the connection process.<br>
	 * The IOHandler is beeing created.
	 * Listeners will be called before the client tries to connect to the server,
	 * when the connection was successful, and when it wasn't.
	 * 
	 * @return true if the connection process as been completed successfully
	 * 
	 * @version 0.0.1-alpha
	 */
	public boolean connect() {
		try {
			notifyListeners(EventType.CONNECTION);
			this.socket = new Socket(this.HOST, this.PORT);
			this.ioHandler = new IOHandler(this.socket.getInputStream(), this.socket.getOutputStream(), this::processReceivedData);
			if(this.socket.isConnected()) {
				notifyListeners(EventType.CONNECTION_SUCCESS);
				this.socket.setKeepAlive(this.keepAlive);
			}
		} catch (Exception e) {
			notifyListeners(EventType.CONNECTION_FAILED);
			return false;
		}
		return true;
	}
	
	/**
	 * Closes the connection.<br>
	 * Listeners will be called before the disconnection process is started
	 * and when it succeeded or failed.<br>
	 * It will also call the close method of the client objects IOHandler
	 * 
	 * @return true if the connection has been closed successfully
	 * 
	 * @see IOHandler#close()
	 * 
	 * @version 0.0.1-alpha
	 */
	public boolean close() {
		try {
			notifyListeners(EventType.CLOSE);
			this.ioHandler.close();
			this.socket.close();
		} catch(Exception e) {
			notifyListeners(EventType.CLOSE_FAILED);
			return false;
		}
		notifyListeners(EventType.CLOSE_SUCCESS);
		return true;
	}
	
	/**
	 * This function will be called once the client receives data from the server.
	 * 
	 * @param data sent by the server
	 */
	protected abstract void processReceivedData(String data);
	
	/**
	 * Calls the respective function of each listener depending on the event type.<br>
	 * Optionally Propagates additional info such as exceptions.
	 * 
	 * @param type of event that occured
	 * @param args optional additional data
	 * 
	 * @throws IllegalArgumentException if additional arguments are supplied
	 * whose types do not match the expected types of the listeners eventfunction
	 * 
	 * @see ClientListener
	 * 
	 * @version 0.0.1-alpha
	 */
	private void notifyListeners(EventType type, Object... args) {
		switch(type) {
			case CONNECTION:
				listeners.forEach(l -> l.onConnection());
				break;
			case CONNECTION_SUCCESS:
				listeners.forEach(l -> l.onConnectionSuccess());
				break;
			case CONNECTION_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onConnectionFailed((Exception)args[0]));
				break;
			case CLOSE:
				listeners.forEach(l -> l.onClose());
				break;
			case CLOSE_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onCloseFailed((Exception)args[0]));
				break;
			case CLOSE_SUCCESS:
				listeners.forEach(l -> l.onCloseSuccess());
				break;
			default:
				break;
		}
	}
	
	/**
	 * Indicates whether the current client object has an active connection to the server
	 * @return true if there is an active connection to the server
	 * 
	 * @version 0.0.1-alpha
	 */
	public boolean isConnected() {
		if(this.socket == null || this.socket.isClosed()) {
			return false;
		}
		return this.socket.isConnected();
	}
	
}
