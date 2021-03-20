package dev.bitbite.networking;

import javax.net.ssl.SSLServerSocketFactory;

public abstract class SSLServer extends Server {

	public SSLServer(int port) {
		super(port);
	}

	@Override
	protected void openServerSocket() {
		try {
			super.serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(super.PORT);
		} catch (Exception e) {
			this.notifyListeners(EventType.START_FAILED, e);
		}
	}
	
	@Override
	protected abstract void processReceivedData(String clientAddress, String data);

}
