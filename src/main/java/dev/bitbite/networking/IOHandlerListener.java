package dev.bitbite.networking;

/**
 * This class contains all functions which, if registered at the IOHandler object, will be called
 * when certain events are happening.
 *
 * @see IOHandler
 *
 * @version 0.0.1-alpha
 */
public abstract class IOHandlerListener {

	/**
	 * Will be called before the process of reading data from the inputStream
	 * is being started
	 */
	public void onDataReadStart() {}

	/**
	 * Will be called once the process of reading data from the inputStream ends
	 */
	public void onDataReadEnd() {}
	
	/**
	 * Will be called if an error occurs while trying to read data from the inputStream
	 * @param exception which was thrown
	 */
	public void onDataReadFailed(Exception exception) {}

	/**
	 * Will be called before the closing of the streams is started
	 */
	public void onCloseStart() {}

	/**
	 * Will be called once the streams have been closed
	 */
	public void onCloseEnd() {}
	
	/**
	 * Will be called if an error occurs while trying to close the streams
	 * @param exception which was thrown during the process
	 */
	public void onCloseFailed(Exception exception) {}

	/**
	 * Will be called before data is sent over the outputStream
	 * @param args which will be sent
	 */
	public void onWrite(byte[] args) {}

	/**
	 * Will be called after the writing to the outputStream has been finished
	 */
	public void onWriteEnd() {}
	
	/**
	 * Will be called if the writing the data to the outputStream fails
	 * @param exception which was thrown while trying to send the data
	 */
	public void onWriteFailed(Exception exception) {}

}
