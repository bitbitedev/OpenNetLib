package dev.bitbite.networking;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This Thread attempts to detect if the server has closed the connection.
 * Every second it checks if since the last read of the server
 * at least {@link #MAX_READ_THRESHOLD} nanoseconds have passed.
 * If so it tries to read 1 byte from its InputChannel which will trigger
 * a disconnection process if the had closed the connection.
 */
public class DisconnectedServerDetector extends Thread {

	private long MAX_READ_THRESHOLD = 5_000_000_000L;
	private Client client;
	private ExecutorService executorService;
	
	public DisconnectedServerDetector(Client client) {
		this.client = client;
		this.executorService = Executors.newSingleThreadExecutor((r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
	}
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			if(client.isClosed()) {
				Thread.currentThread().interrupt();
				continue;
			}
			if(client.getIOHandler().getTimeSinceLastRead() > MAX_READ_THRESHOLD) {
				Future<Boolean> future = executorService.submit(() -> {
					Thread.currentThread().setName("[DDS] server checker");
					client.getIOHandler().readNBytes(1);
					return true;
				});
				try {
					future.get(20, TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					future.cancel(true);
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		Thread.currentThread().interrupt();
	}
	
	/**
	 * Sets the minimum time in nanoseconds to wait since the last read to check for disconnection.
	 * Default is 5 seconds (5.000.000.000 nano seconds)
	 * @param threshold
	 */
	public void setMaxReadThreshold(long threshold) {
		this.MAX_READ_THRESHOLD = threshold;
	}
	
}
