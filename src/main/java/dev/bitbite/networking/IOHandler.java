package dev.bitbite.networking;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

/**
 * This IOHandler class combines an input- and an output stream object into a single class.
 * Incoming data of the inputstream will be propagated to the onRead consumer method passed
 * as an argument to the constructor.<br>
 * You can write to the outputstream via {@link #write(byte[])}<br>
 * The process of reading data must be initiated using {@link #read()}. 
 * This is necessary to make it possible to read data of multiple IOHandlers within a single
 * thread without any IOHandler blocking the process.
 */
public class IOHandler {

	private static byte END_OF_MESSAGE_BYTE = 0x0A;
	private static int MAX_READ_SIZE = 1024;
	@Getter @Setter private static boolean VERBOSE = false;
	
	private boolean closing = false;
	@Getter private boolean closed = false;
	private InputStream inputStream;
	private OutputStream outputStream;
	private ArrayList<Byte> readBuffer;
	private Consumer<byte[]> readCallback;
	private ArrayList<IOHandlerListener> listeners;
	private long lastRead;
	
	/**
	 * The different event-types, which occur in the IOHandler, listeners can listen on
	 * 
	 * @see IOHandlerListener
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
	 * @param inputStream, the inputStream to read the data from
	 * @param outputStream, the outputStream to write to
	 * @param onRead, the read Callback method which is called when a message is received
	 * 
	 * @throws IllegalArgumentException if at least one of the supplied arguments is null
	 */
	public IOHandler(InputStream inputStream, OutputStream outputStream, Consumer<byte[]> onRead) {
		if(inputStream == null || outputStream == null || onRead == null) {
			throw new IllegalArgumentException("Parameters of IOHandler constructor must not be null");
		}
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.readBuffer = new ArrayList<Byte>();
		this.readCallback = onRead;
		this.listeners = new ArrayList<IOHandlerListener>();
		this.lastRead = System.nanoTime();
	}
	
	/**
	 * Closes the streams
	 */
	public void close() {
		if(closing || closed) {
			return;
		}
		closing = true;
		this.notifyListeners(EventType.CLOSE_START);
		try {
			this.outputStream.flush();
			this.outputStream.close();
			closed = true;
		} catch(Exception e) {
			this.notifyListeners(EventType.CLOSE_FAILED, e);
		}
		this.notifyListeners(EventType.CLOSE_END);
	}
	
	/**
	 * Reads data from the inputstream if there is data available.
	 */
	public void read() {
		if(closing || closed) {
			return;
		}
		try {
			if(inputStream.available() > 0) {
				readNBytes(MAX_READ_SIZE);
			}
		} catch (SocketException e) {
			if(e.getMessage().contains("Connection reset") || e.getMessage().contains("Socket closed")) {
				close();
			} else {
				this.notifyListeners(EventType.DATA_READ_FAILED, e);
			}
		} catch (Exception e) {
			this.notifyListeners(EventType.DATA_READ_FAILED, e);
		}
	}
	
	/**
	 * Tries to read a set amount of bytes from the stream. 
	 * If an end-of-message byte is detected the read bytes are passed to the
	 * read callback. 
	 * If an end of stream is detected the IOHandler is closed.
	 * If more bytes are available than the amount that should be read they are left
	 * in the stream until the next call
	 * @param amount of bytes to read
	 */
	protected void readNBytes(int amount) {
		if(closing || closed) {
			return;
		}
		try {
			for(int i = 0; i < amount; i++) {
				int iRead = inputStream.read();
				this.lastRead = System.nanoTime();
				if(iRead == -1) {
					close();
					return;
				}
				byte read = ((Integer)iRead).byteValue();
				if(read != IOHandler.END_OF_MESSAGE_BYTE) {
					readBuffer.add(read);
				} else {
					flushRead();
					break;
				}
			}
		} catch (SocketException e) {
			if(e.getMessage().contains("Connection reset") || e.getMessage().contains("Socket closed")) {
				close();
			} else {
				this.notifyListeners(EventType.DATA_READ_FAILED, e);
			}
		} catch (Exception e) {
			this.notifyListeners(EventType.DATA_READ_FAILED, e);
		}
	}
	
	/**
	 * Reads bytes until the buffer contains the given amount of bytes
	 * @param total number of bytes to be read to the buffer until it gets flushed
	 */
	protected void readToNBytes(int total) {
		while(readBuffer.size() < total) {
			readNBytes(1);
		}
		flushRead();
	}
	
	/**
	 * Calls the readCallback with the bytes currently contained in the buffer
	 */
	protected void flushRead() {
		byte[] result = new byte[readBuffer.size()];
		for(int i = 0; i < readBuffer.size(); i++) {
		    result[i] = readBuffer.get(i).byteValue();
		}
		readBuffer.clear();
		readCallback.accept(result);
	}
	
	/**
	 * Writes data to the OutputStream and flushes it.
	 * @param data to be send
	 * @see java.io.PrintWriter
	 */
	public void write(byte[] data) {
		if(closing || closed) {
			return;
		}
		this.notifyListeners(EventType.WRITE, data);
		try {
			this.outputStream.write(data);
			this.outputStream.write('\n');
			this.outputStream.flush();
		} catch(Exception e) {
			this.notifyListeners(EventType.WRITE_FAILED, e);
		}
		this.notifyListeners(EventType.WRITE_END);
	}

	/**
	 * Registers a ClientListener
	 * @param listener to add
	 */
	public void registerListener(IOHandlerListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Removes ClientListener from the listeners
	 * @param listener to remove
	 */
	public void removeListener(IOHandlerListener listener) {
		if(this.listeners.contains(listener)) {
			this.listeners.remove(listener);
		}
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
	 */
	private void notifyListeners(EventType type, Object... args) {
		if(IOHandler.VERBOSE && args.length > 0 && args[0] instanceof Exception) {
			((Exception)args[0]).printStackTrace();
		}
		switch(type) {
			case DATA_READ_START:
				listeners.forEach(l -> l.onDataReadStart());
				break;
			case DATA_READ_END:
				listeners.forEach(l -> l.onDataReadEnd());
				break;
			case DATA_READ_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
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
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onCloseFailed((Exception)args[0]));
				break;
			case WRITE:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type byte[], but got nothing");
				} else if(!(args[0] instanceof byte[])) {
					throw new IllegalArgumentException("Expected object of type byte[], but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onWrite((byte[])args[0]));
				break;
			case WRITE_END:
				listeners.forEach(l -> l.onWriteEnd());
				break;
			case WRITE_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				listeners.forEach(l -> l.onWriteFailed((Exception)args[0]));
				break;
		}
	}
	
	/**
	 * Returns the time that has passed since the last read in nanoseconds
	 * @return the time that has passed since the last read in nanoseconds
	 */
	public long getTimeSinceLastRead() {
		return System.nanoTime() - this.lastRead;
	}
	
	/**
	 * Returns the byte that is currently set to mark the end of a message.
	 * Its default value is set to 0x0A, which is the LINE FEED byte.
	 * @return the byte that is currently set to mark the end of a message
	 */
	public static byte getEndOfMessageByte() {
		return IOHandler.END_OF_MESSAGE_BYTE;
	}
	
	/**
	 * Sets the byte that marks the end of a message.
	 * Its default value is set to 0x0A, which is the LINE FEED byte.
	 * @param endOfMessageByte the byte that should represent the end of a message
	 */
	public static void setEndOfMessageByte(byte endOfMessageByte) {
		IOHandler.END_OF_MESSAGE_BYTE = endOfMessageByte;
	}
	
	/**
	 * Returns the maximum count of bytes that are being read at one time.
	 * This is done so that a long message does not block other connections from reading for too long. 
	 * @return the maximum count of bytes that are being read at one time.
	 */
	public static int getMaxReadSize() {
		return IOHandler.MAX_READ_SIZE;
	}
	
	/**
	 * Sets the maximum count of bytes that are being read at one time.
	 * This is done so that a long message does not block other connections from reading for too long.
	 * Higher values may result in longer answer times.
	 * @param maxReadSize the maximum count of bytes that are being read at one time.
	 */
	public static void setMaxReadSize(int maxReadSize) {
		IOHandler.MAX_READ_SIZE = maxReadSize;
	}
}
