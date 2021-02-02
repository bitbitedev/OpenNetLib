package dev.bitbite.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
		this.inputListener = new Thread(()->{
			while(!Thread.interrupted()) {
				try {
					String message = reader.readLine();
					onRead.accept(message);
				} catch (IOException e) {
					//TODO add Handler
					e.printStackTrace();
				}
			}
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
		this.reader.close();
		this.writer.close();
	}
	
	/**
	 * Writes data to the OutputStream and flushes it.
	 * @param data to be send
	 * @version 0.0.1-alpha
	 * @see java.io.PrintWriter
	 */
	public void write(String data) {
		this.writer.write(data+"\n");
		this.writer.flush();
	}
	
}
