package mutex.app.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import mutex.app.utils.Utils;

public class Server1 {
	static int i = 1;

	public static void main(String[] args) throws IOException {
		ServerSocket ss = null;
		Socket s = null;
		try {
			ss = new ServerSocket(6666);
			while (true) {
				s = ss.accept();
				Utils.log("Server1: New client request received : " + s);
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				ServerHandler mtch = new ServerHandler("new handler for client " + i, dis, dos, s);
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