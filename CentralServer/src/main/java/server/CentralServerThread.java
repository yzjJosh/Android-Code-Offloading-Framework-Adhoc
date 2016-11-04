package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CentralServerThread extends Thread {
	private final int port;
	private ServerSocket serverSocket;
	private boolean stopSign;
	private CentralServer centralServer;

	public CentralServerThread(int port, CentralServer centralServer) {
		this.port = port;
		this.stopSign = false;
		this.centralServer = centralServer;
	}

	@Override
	public void run() {
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (serverSocket != null) {
			try {
				while (!stopSign) {
					Socket socket = serverSocket.accept();
					Work worker = new Work(socket);
					executor.submit(worker);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void kill() throws IOException {
		stopSign = true;
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	private class Work implements Runnable {
		private final Socket socket;

		public Work(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				centralServer.serve(socket);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
