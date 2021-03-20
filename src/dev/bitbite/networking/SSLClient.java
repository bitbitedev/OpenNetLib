package dev.bitbite.networking;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

public abstract class SSLClient extends Client {

	public SSLClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void openSocket() throws UnknownHostException, IOException {
		super.socket = SSLSocketFactory.getDefault().createSocket(super.HOST, super.PORT);
	}
	
	@Override
	protected abstract void processReceivedData(String data);

}
