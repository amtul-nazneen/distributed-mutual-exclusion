package mutex.app.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class Server {
	static int i = 1;

	public static void main(String[] args) throws IOException {
		ServerSocket ss = null;
		Socket s = null;

		try {
			ss = new ServerSocket(6666);
			// Utils.log("Server:" + ss.getInetAddress());
			while (true) {
				s = ss.accept();
				Utils.log("New client request received : " + s.getInetAddress());
				ServerHandler mtch = new ServerHandler("new handler for client " + i, s, Config.SERVER_1);
				Thread t = new Thread(mtch);
				t.start();
				i++;
			}
		} catch (Exception e) {
			System.out.println(e);
			if (ss != null)
				ss.close();
		}
	}
}