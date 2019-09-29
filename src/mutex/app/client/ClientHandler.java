package mutex.app.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

import mutex.app.impl.MutualExclusionImpl;
import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class ClientHandler implements Runnable {
	BufferedReader reader;
	PrintWriter writer;
	Socket socket;
	MutualExclusionImpl mutexImpl;

	public ClientHandler(Socket s, MutualExclusionImpl mutexImpl) {
		super();
		this.socket = s;
		this.mutexImpl = mutexImpl;
		try {
			InputStreamReader iReader = new InputStreamReader(s.getInputStream());
			reader = new BufferedReader(iReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		{
			String message;
			try {
				while ((message = reader.readLine()) != null) {

					String tokens[] = message.split(",");
					String messageType = tokens[0];

					String host = socket.getInetAddress().getHostName().toUpperCase();
					host = Utils.getProcessFromHost(host);

					if (messageType.equals(Config.REQUEST)) {
						mutexImpl.myReceivedRequest(Timestamp.valueOf(tokens[1]), Integer.parseInt(tokens[2]),
								tokens[3]);
					} else if (messageType.equals(Config.REPLY)) {
						Utils.log("$$$-->Received [REPLY]" + " from " + host);
						mutexImpl.myReceivedReply();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
