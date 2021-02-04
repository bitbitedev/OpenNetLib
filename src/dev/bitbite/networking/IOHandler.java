package dev.bitbite.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * This IOHandler class combines an input- and an output stream object into a single class.
 * Incoming data of the inputstream will be propagated to the onRead consumer method passed
 * as an argument to the constructor.<br>
 * You can write to the outputstream via {@link #write(String)}<br>
 * <br>
 * For the process of reading incoming data from the InputStream a Thread is started and named "IO InputListener"
 * 
 * @version 0.0.2-alpha
 */
public class IOHandler {

	private PrintWriter writer;
	private BufferedReader reader;
	private Thread inputListener;
	private ArrayList<IOHandlerListener> listeners;
	
	/**
	 * The different event-types, which occur in the IOHandler, listeners can listen on
	 * 
	 * @see IOHandlerListener
	 * @version 0.0.1-alpha
	 */
	enum EventType {
		DATA_READ_START,
		DATA_READ_END,
		DATA_READ_FAILED,
		
		CLOSE_START,
		CLOSE_END,
		CLOSE_FAILED,
		
		WRITE,
		WRITE_END,
		WRITE_FAILED
	}
	
	/**
	 * Initializes the IOHandler with the given Streams and read-Callback method.<br>
	 * It also starts a Thread which listens for incomming data from the inputStream.
	 * That is done so the IOHandler is non-blocking.
	 * @param inputStream, the inputStream to read the data from
	 * @param outputStream, the outputStream to write to
	 * @param onRead, the read Callback method which is called when a message is received
	 * 
	 * @throws IllegalArgumentException if at least one of the supplied arguments is null
	 * 
	 * @version 0.0.2-alpha
	 */
	public IOHandler(InputStream inputStream, OutputStream outputStream, Consumer<String> onRead) {
		if(inputStream == null || outputStream == null || onRead == null) {
			throw new IllegalArgumentException("Parameters of IOHandler constructor must not be null");
		}
		this.writer = new PrintWriter(outputStream);
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
		this.listeners = new ArrayList<IOHandlerListener>();
		this.inputListener = new Thread(()->{
			notifyListeners(EventType.DATA_READ_START);
			while(!Thread.interrupted()) {
				try {
					String message = reader.readLine();
					onRead.accept(message);
				} catch (IOException e) {
					notifyListeners(EventType.DATA_READ_FAILED);
					e.printStackTrace();
				}
			}
			notifyListeners(EventType.DATA_READ_END);
		});
		this.inputListener.setName("IO InputListener");
		this.inputListener.start();
	}
	
	/**
	 * Closes the streams
	 * @throws IOException if any closing fails
	 * @version 0.0.2-alpha
	 */
	public void close() throws IOException {
		notifyListeners(EventType.CLOSE_START);
		try {
			this.reader.close();
			this.writer.close();
		} catch(Exception e) {
			notifyListeners(EventType.CLOSE_FAILED);
		}
		notifyListeners(EventType.CLOSE_END);
	}
	
	/**
	 * Writes data to the OutputStream and flushes it.
	 * @param data to be send
	 * @version 0.0.1-alpha
	 * @see java.io.PrintWriter
	 */
	public void write(String data) {
		notifyListeners(EventType.WRITE);
		try {
			this.writer.write(data+"\n");
			this.writer.flush();
		} catch(Exception e) {
			notifyListeners(EventType.WRITE_FAILED);
		}
		notifyListeners(EventType.WRITE_END);
	}
	
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
	 * @see IOHandlerListener
	 * 
	 * @version 0.0.1-alpha
	 */
	private void notifyListeners(EventType type, Object... args) {
		switch(type) {
			case DATA_READ_START:
				listeners.forEach(l -> l.onDataReadStart());
				break;
			case DATA_READ_END:
				listeners.forEach(l -> l.onDataReadEnd());
				break;
			case DATA_READ_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onDataReadFailed((Exception)args[0]));
				break;
			case CLOSE_START:
				listeners.forEach(l -> l.onCloseStart());
				break;
			case CLOSE_END:
				listeners.forEach(l -> l.onCloseEnd());
				break;
			case CLOSE_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onCloseFailed((Exception)args[0]));
				break;
			case WRITE:
				if(!(args[0] instanceof String)) {
					throw new IllegalArgumentException("Expected object of type String, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onWrite((String)args[0]));
				break;
			case WRITE_END:
				listeners.forEach(l -> l.onWriteEnd());
				break;
			case WRITE_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onWriteFailed((Exception)args[0]));
				break;
		}
	}
	
}
