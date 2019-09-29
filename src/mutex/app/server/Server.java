package mutex.app.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class Server {
	static int i = 1;

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			Utils.log("No Server ID provided.");
			return;
		}
		String id = args[0];
		String serverName = null;
		if ("1".equals(id)) {
			serverName = Config.SERVER_1;
			Utils.log("Starting Server:1");
		} else if ("2".equals(id)) {
			serverName = Config.SERVER_2;
			Utils.log("Starting Server:2");
		} else if ("3".equals(id)) {
			serverName = Config.SERVER_3;
			Utils.log("Starting Server:3");
		}

		ServerSocket ss = null;
		Socket s = null;

		try {
			ss = new ServerSocket(Config.SERVER_PORT);
			while (true) {
				s = ss.accept();
				Utils.log("Client:" + i + " connected");
				ServerHandler clientThread = new ServerHandler(s, serverName);
				Thread t = new Thread(clientThread);
				t.start();
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (ss != null)
				ss.close();
		} finally {
			Utils.log("Closing the socket");
			ss.close();
		}
	}
}