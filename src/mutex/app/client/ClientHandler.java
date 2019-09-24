package mutex.app.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import mutex.app.impl.MutualExclusionImpl;
import mutex.app.utils.Utils;

public class ClientHandler implements Runnable {
	BufferedReader reader;
	PrintWriter writer;
	Socket s;
	MutualExclusionImpl meimpl;

	public ClientHandler(Socket s, MutualExclusionImpl meimpl) {
		super();
		this.s = s;
		this.meimpl = meimpl;
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
					String host = s.getInetAddress().getHostName().toUpperCase();
					host = Utils.getProcessFromHost(host);
					if (messageType.equals("REPLY"))
						Utils.log("$$$-->Received [" + messageType + "]" + " from " + host);
					if (messageType.equals("REQUEST")) {
						meimpl.receiveRequest(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
					} else if (messageType.equals("REPLY")) {
						meimpl.receiveReply();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
