package mutex.app.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ServerHandler implements Runnable {
	final DataInputStream dis;
	final DataOutputStream dos;
	String name;
	Socket s;

	public ServerHandler(String name, DataInputStream dis, DataOutputStream dos, Socket s) {
		super();
		this.dis = dis;
		this.dos = dos;
		this.s = s;
		this.name = name;
	}

	@Override
	public void run() {
		System.out.println("Running a new thread for " + this.name);
	}

}
