package dev.bitbite.networking;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DisconnectedClientDetector extends Thread {

	private long MAX_READ_THRESHOLD = 5_000_000_000L;
	private Server server;
	private ExecutorService executorService;
	
	public DisconnectedClientDetector(Server server) {
		this.server = server;
		this.executorService = Executors.newSingleThreadExecutor((r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
	}
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			for(var ch : server.getClientManager().getCommunicationHandler()) {
				if(ch.getIOHandler().getTimeSinceLastRead() > MAX_READ_THRESHOLD) {
					Future<Boolean> future = executorService.submit(() -> {
						Thread.currentThread().setName("[DDC] client checker");
						ch.getIOHandler().readNBytes(1);
						return true;
					});
					try {
						future.get(20, TimeUnit.MILLISECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						future.cancel(true);
					}
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
