package mutex.app.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class Client4 {

	MutualExclusionImpl meimpl;
	int processnum = 4;
	int counter = 0;

	Socket server1 = null, server2 = null, server3 = null;
	Socket s2, s1, s3, s5;
	ServerSocket ss5;

	PrintWriter w1, w2, w3, w5;
	BufferedReader r1, r2, r3, r5;

	public static void main(String[] args) throws IOException {
		Client4 client4 = new Client4();
		try {
			client4.startClient4();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startClient4() throws Exception {

		try {
			// connects to the server1, server2, server3
			server1 = new Socket("dc01.utdallas.edu", 6666);
			// server2 = new Socket("dc02.utdallas.edu", 6666);
			// server3 = new Socket("dc03.utdallas.edu", 6666);

			// connects to client1, client2
			s1 = new Socket("dc04.utdallas.edu", 6664);
			s2 = new Socket("dc05.utdallas.edu", 6664);
			s3 = new Socket("dc06.utdallas.edu", 6664);

			// creates sockets for client5
			ss5 = new ServerSocket(6665);
			s5 = ss5.accept();

			w1 = new PrintWriter(s1.getOutputStream(), true);
			r1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
			w2 = new PrintWriter(s2.getOutputStream(), true);
			r2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
			w3 = new PrintWriter(s3.getOutputStream(), true);
			r3 = new BufferedReader(new InputStreamReader(s3.getInputStream()));
			w5 = new PrintWriter(s5.getOutputStream(), true);
			r5 = new BufferedReader(new InputStreamReader(s5.getInputStream()));

			meimpl = new MutualExclusionImpl(processnum, 0);
			meimpl.w[0] = w1;
			meimpl.w[1] = w2;
			meimpl.w[2] = w3;
			meimpl.w[3] = w5;

			ClientHandler css1 = new ClientHandler(s1, meimpl);
			ClientHandler css2 = new ClientHandler(s2, meimpl);
			ClientHandler css3 = new ClientHandler(s3, meimpl);
			ClientHandler css5 = new ClientHandler(s5, meimpl);
			Thread t1 = new Thread(css1);
			Thread t2 = new Thread(css2);
			Thread t3 = new Thread(css3);
			Thread t5 = new Thread(css5);
			t1.start();
			t2.start();
			t3.start();
			t5.start();
			while (counter < Config.CLIENT4_CSLIMIT) {
				try {
					requestCS();
					counter++;
					Random num = new Random();
					Thread.sleep(num.nextInt(500));
				} catch (Exception e) {
					Utils.log(e.getMessage());
				}
			}
			Utils.log("Finished CS Limit, Process:" + processnum);

		} catch (Exception e) {
			Utils.log(e.getMessage());
			server1.close();
			// server2.close();
			// server3.close();
		}
	}

	public void requestCS() {
		int attempt = counter + 1;
		Utils.log("Entering RequestCS, Process:" + processnum + " #CS_Access: " + attempt);
		meimpl.invocation();
		criticalSection(processnum, counter);
		meimpl.releaseCS();
		Utils.log("Exiting RequestCS, Process:" + processnum + " #CS_Access: " + attempt);
	}

	public void criticalSection(int processnum, int counter) {
		int attempt = counter + 1;
		Utils.log("***>> Starting CS - Process: " + processnum + " #CS_Access: " + attempt);
		try {
			Thread.sleep(Config.CLIENT4_CSEXEC);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Utils.log("***>> Completed CS - Process: " + processnum + " #CS_Access: " + attempt);
	}

}
