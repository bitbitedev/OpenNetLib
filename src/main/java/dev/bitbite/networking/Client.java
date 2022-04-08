package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import dev.bitbite.networking.DataPreProcessor.TransferMode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an abstract implementation of the client-side connection.<br>
 * The connection process can be initiated using the client objects {@link #connect()} method, 
 * which will return true, if the connection process has been successful.<br>
 * Closing the connection can be done using the client objects {@link #close()} method,
 * which will return true, if the disconnection process has been completed successfully.<br>
 * Incoming data from the server will be preprocessed by {@link #preprocessReceivedData(byte[])}
 * and then forwarded to {@link #processReceivedData(byte[])}.<br>
 * Data to the server can be sent using the clients {@link IOHandler}.<br>
 * Some events trigger the notification of registered {@link ClientListener}s. 
 * 
 * @see IOHandler
 * @see ClientListener
 */
public abstract class Client {

	@Getter @Setter private boolean closed;
	public final String HOST;
	public final int PORT;
	protected Socket socket;
	@Getter private IOHandler iOHandler;
	@Getter private DataPreProcessor dataPreProcessor;
	protected DisconnectedServerDetector disconnectedServerDetector;
	private Thread readThread;
	private boolean keepAlive = false;
	private ArrayList<ClientListener> listeners;
	private ArrayList<IOHandlerListener> ioListeners;
	
	/**
	 * The different event-types, which occur in the client, listeners can listen on
	 * 
	 * @see ClientListener
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
		this.listeners = new ArrayList<ClientListener>();
		this.ioListeners = new ArrayList<IOHandlerListener>();
		this.dataPreProcessor = new DataPreProcessor();
		this.ioListeners.add(new ClientCloseListener(this));
		this.disconnectedServerDetector = new DisconnectedServerDetector(this);
		this.disconnectedServerDetector.setName("Disconnected Server Detector");
	}
	
	/**
	 * Creates a Client object and sets the endpoint to which the client will connect on startup.
	 * @param url the URL object that specifies the endpoint adress and port.
	 */
	public Client(URL url) {
		this(url.getHost(), url.getPort());
	}
	
	/**
	 * Initiates the connection process.<br>
	 * The IOHandler will be created.
	 * Listeners will be called before the client tries to connect to the server,
	 * when the connection was successful, and when it wasn't.
	 * 
	 * @return true if the connection process as been completed successfully
	 */
	public boolean connect() {
		try {
			this.notifyListeners(EventType.CONNECTION);
			this.openSocket();
			this.iOHandler = new IOHandler(this.socket.getInputStream(), this.socket.getOutputStream(), this::preprocessReceivedData);
			this.ioListeners.forEach(l -> this.iOHandler.registerListener(l));
			if(this.socket.isConnected()) {
				this.notifyListeners(EventType.CONNECTION_SUCCESS);
				this.socket.setKeepAlive(this.keepAlive);
			}
			if(this.readThread != null) {
				this.readThread.interrupt();
			}
			this.readThread = new Thread(()->{
				while(!Thread.interrupted()) {
					this.iOHandler.read();
				}
				Thread.currentThread().interrupt();
			});
			this.readThread.setName("Data reader");
			this.readThread.start();
			this.disconnectedServerDetector.start();
		} catch (Exception e) {
			this.notifyListeners(EventType.CONNECTION_FAILED, e);
			return false;
		}
		return true;
	}
	
	/**
	 * Opens the Socket connection.
	 * Moved to a different function to make it easier to replace the Socket implementation.
	 * @throws UnknownHostException if the specified Host is unknown
	 * @throws IOException when the process of opening the Socket fails.
	 */
	protected void openSocket() throws UnknownHostException, IOException {
		this.socket = new Socket(this.HOST, this.PORT);
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
	 */
	public boolean close() {
		try {
			this.notifyListeners(EventType.CLOSE);
			this.readThread.interrupt();
			this.iOHandler.close();
			this.socket.close();
			this.disconnectedServerDetector.interrupt();
		} catch(Exception e) {
			this.notifyListeners(EventType.CLOSE_FAILED, e);
			return false;
		}
		this.notifyListeners(EventType.CLOSE_SUCCESS);
		closed = true;
		return true;
	}
	
	/**
	 * Sends data to the Server
	 * @param data to send
	 */
	public void send(byte[] data) {
		data = dataPreProcessor.process(TransferMode.OUT, data);
		this.iOHandler.write(data);
	}
	
	/**
	 * Preprocesses incomming data by sending it to the {@link DataPreProcessor}.
	 * The processed data is then forwarded to {@link #processReceivedData(byte[])};
	 * @param data to process
	 */
	protected void preprocessReceivedData(byte[] data) {
		processReceivedData(dataPreProcessor.process(TransferMode.IN, data));
	}
	
	/**
	 * This function will be called once the client receives data from the server.
	 * 
	 * @param data sent by the server
	 */
	protected abstract void processReceivedData(byte[] data);
	
	/**
	 * Registers a ClientListener
	 * @param listener to add
	 */
	public void registerListener(ClientListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Registers a IOHandlerListener
	 * @param listener to add
	 */
	public void registerListener(IOHandlerListener listener) {
		this.ioListeners.add(listener);
		if(this.iOHandler != null) {
			this.iOHandler.registerListener(listener);
		}
	}
	
	/**
	 * Removes ClientListener from the listeners
	 * @param listener to remove
	 */
	public void removeListener(ClientListener listener) {
		if(this.listeners.contains(listener)) {
			this.listeners.remove(listener);
		}
	}
	
	/**
	 * Removes IOHandlerListener from the listeners
	 * @param listener to remove
	 */
	public void removeListener(IOHandlerListener listener) {
		if(this.ioListeners.contains(listener)) {
			this.ioListeners.remove(listener);
			this.iOHandler.removeListener(listener);
		}
	}
	
	/**
	 * Calls the respective function of each listener depending on the event type.<br>
	 * Optionally propagates additional info such as exceptions.
	 * 
	 * @param type of event that occured
	 * @param args optional additional data
	 * 
	 * @throws IllegalArgumentException if additional arguments are supplied
	 * whose types do not match the expected types of the listeners eventfunction
	 * 
	 * @see ClientListener
	 */
	private void notifyListeners(EventType type, Object... args) {
		switch(type) {
			case CONNECTION:
				this.listeners.forEach(l -> l.onConnectionCreation());
				break;
			case CONNECTION_SUCCESS:
				this.listeners.forEach(l -> l.onConnectionSuccess());
				break;
			case CONNECTION_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onConnectionFailed((Exception)args[0]));
				break;
			case CLOSE:
				this.listeners.forEach(l -> l.onCloseRequested());
				break;
			case CLOSE_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(args.length == 0 || !(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCloseFailed((Exception)args[0]));
				break;
			case CLOSE_SUCCESS:
				this.listeners.forEach(l -> l.onCloseSuccess());
				break;
			default:
				break;
		}
	}
	
	/**
	 * Indicates whether the current client object has an active connection to the server
	 * @return true if there is an active connection to the server
	 */
	public boolean isConnected() {
		if(this.socket == null || this.socket.isClosed()) {
			return false;
		}
		return this.socket.isConnected();
	}
	
}
