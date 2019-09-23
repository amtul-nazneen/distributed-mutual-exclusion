package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import mutex.app.utils.Utils;

public class Server1Test {
	static int i = 1;

	public static void main(String[] args) throws IOException {
		ServerSocket ss = null;
		Socket s = null;
		try {
			ss = new ServerSocket(6666);
			while (true) {
				s = ss.accept();
				Utils.log("Server1: New client request received : " + s);
				ServerHandlerTest mtch = new ServerHandlerTest("new handler for client " + i, s);
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