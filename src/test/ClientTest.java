package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import mutex.app.runner.MutualExclusionImpl;
import mutex.app.utils.Utils;

public class ClientTest {
	MutualExclusionImpl meimpl;
	int processnum = 1;
	int counter = 0;

	Socket server1 = null, server2 = null, server3 = null;
	Socket s2, s3, s4, s5;
	ServerSocket ss2, ss3, ss4, ss5;

	PrintWriter w2, w3, w4, w5;
	BufferedReader r2, r3, r4, r5;

	public static void main(String[] args) throws IOException {
		ClientTest client1 = new ClientTest();
		try {
			client1.startClient1();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startClient1() throws Exception {
		try {
			server1 = new Socket("dc01.utdallas.edu", 6666);
			w2 = new PrintWriter(server1.getOutputStream(), true);
			r2 = new BufferedReader(new InputStreamReader(server1.getInputStream()));

			try {
				requestCSread();
				requestCSwrite();
				requestCSread();
				counter++;

			} catch (Exception e) {
				Utils.log(e.getMessage());
			}
			Utils.log("Finished CS Limit, Process:" + processnum);
		} catch (Exception e) {
			Utils.log(e.getMessage());
			server1.close();
		}
	}

	public void requestCSread() throws Exception {
		Utils.log("Reading from server");
		w2.println("read,file1");
		String reply;
		Utils.log("Sent the request, Waiting for reply");
		boolean gotReply = false;
		while (!gotReply) {

			reply = r2.readLine();
			if (reply != null) {
				Utils.log("Received reply:-->" + reply);
				gotReply = true;
			}
		}
		Utils.log("End of read critical section");
	}

	public void requestCSwrite() throws Exception {
		Utils.log("Writing to server");
		w2.println("write,file1,iwrotethis");
		String reply;
		Utils.log("Sent the request, Waiting for reply");
		boolean gotReply = false;
		while (!gotReply) {

			reply = r2.readLine();
			if (reply != null) {
				Utils.log("Received reply:-->" + reply);
				gotReply = true;
			}
		}
		Utils.log("End of write critical section");
	}

}
