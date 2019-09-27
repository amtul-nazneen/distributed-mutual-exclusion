package mutex.app.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import mutex.app.impl.MutualExclusionImpl;
import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class Client5 {

	MutualExclusionImpl meimpl;
	int processnum = 5;
	int counter = 0;

	Socket server1 = null, server2 = null, server3 = null;
	Socket s2, s1, s3, s4;
	ServerSocket ss5;

	PrintWriter w1, w2, w3, w4;
	BufferedReader r1, r2, r3, r4;
	PrintWriter writeToServer1, writeToServer2, writeToServer3;
	BufferedReader readFromServer1, readFromServer2, readFromServer3;

	ArrayList<String> serverFiles;

	public static void main(String[] args) throws IOException {
		Client5 client5 = new Client5();
		try {
			client5.startClient5();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startClient5() throws Exception {

		try {
			// connects to the server1, server2, server3
			server1 = new Socket("dc01.utdallas.edu", 6666);
			server2 = new Socket("dc02.utdallas.edu", 6666);
			server3 = new Socket("dc03.utdallas.edu", 6666);

			// connects to client1, client2, client3, client4
			s1 = new Socket("dc04.utdallas.edu", 6665);
			s2 = new Socket("dc05.utdallas.edu", 6665);
			s3 = new Socket("dc06.utdallas.edu", 6665);
			s4 = new Socket("dc07.utdallas.edu", 6665);

			writeToServer1 = new PrintWriter(server1.getOutputStream(), true);
			readFromServer1 = new BufferedReader(new InputStreamReader(server1.getInputStream()));
			writeToServer2 = new PrintWriter(server2.getOutputStream(), true);
			readFromServer2 = new BufferedReader(new InputStreamReader(server2.getInputStream()));
			writeToServer3 = new PrintWriter(server3.getOutputStream(), true);
			readFromServer3 = new BufferedReader(new InputStreamReader(server3.getInputStream()));

			w1 = new PrintWriter(s1.getOutputStream(), true);
			r1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
			w2 = new PrintWriter(s2.getOutputStream(), true);
			r2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
			w3 = new PrintWriter(s3.getOutputStream(), true);
			r3 = new BufferedReader(new InputStreamReader(s3.getInputStream()));
			w4 = new PrintWriter(s4.getOutputStream(), true);
			r4 = new BufferedReader(new InputStreamReader(s4.getInputStream()));

			meimpl = new MutualExclusionImpl(processnum, 0);
			meimpl.w[0] = w1;
			meimpl.w[1] = w2;
			meimpl.w[2] = w3;
			meimpl.w[3] = w4;

			ClientHandler css1 = new ClientHandler(s1, meimpl);
			ClientHandler css2 = new ClientHandler(s2, meimpl);
			ClientHandler css3 = new ClientHandler(s3, meimpl);
			ClientHandler css4 = new ClientHandler(s4, meimpl);
			Thread t1 = new Thread(css1);
			Thread t2 = new Thread(css2);
			Thread t3 = new Thread(css3);
			Thread t4 = new Thread(css4);
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			while (counter < Config.CLIENT5_CSLIMIT) {
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
			server2.close();
			server3.close();
		}
	}

	public void requestCS() throws Exception {
		int attempt = counter + 1;
		Utils.log("Entering RequestCS, Process:" + processnum + " #CS_Access: " + attempt);
		meimpl.invocation();
		criticalSection(processnum, counter);
		meimpl.releaseCS();
		Utils.log("Exiting RequestCS, Process:" + processnum + " #CS_Access: " + attempt);
	}

	public void criticalSection(int processnum, int counter) throws Exception {
		int attempt = counter + 1;
		Utils.log("***>> Starting CS - Process: " + processnum + " #CS_Access: " + attempt);
		try {
			Utils.log("Client5 doing a write");
			writeToServer();
			Thread.sleep(Config.CLIENT5_CSEXEC);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Utils.log("***>> Completed CS - Process: " + processnum + " #CS_Access: " + attempt);
	}

	public void readFromServer() throws Exception {
		Utils.log("Reading from server");
		writeToServer1.println("read,file1");
		String reply;
		Utils.log("Sent the request, Waiting for reply");
		boolean gotReply = false;
		while (!gotReply) {

			reply = readFromServer1.readLine();
			if (reply != null) {
				Utils.log("Received reply:-->" + reply);
				gotReply = true;
			}
		}
	}

	public void writeToServer() throws Exception {
		Utils.log("Writing to server");
		writeToServer1.println("write,file1," + Config.WRITE_MESSAGE + processnum);
		String reply;
		Utils.log("Sent the request, Waiting for reply");
		boolean gotReply = false;
		while (!gotReply) {

			reply = readFromServer1.readLine();
			if (reply != null) {
				Utils.log("Received reply:-->" + reply);
				gotReply = true;
			}
		}
	}

	public void enquireToServer() throws Exception {
		Utils.log("Enquiring from server");
		writeToServer3.println("enquire," + processnum);
		Utils.log("Sent the enquire, waiting for reply");
		boolean gotReply = false;
		String reply = "";
		while (!gotReply) {
			reply = readFromServer3.readLine();
			if (reply != null) {
				Utils.log("Received reply-->:" + reply);
				gotReply = true;
			}
		}
		Utils.log("Saving the list of available server files");
		serverFiles = new ArrayList<String>();
		String files[] = reply.split(",");
		for (int i = 0; i < files.length; i++)
			serverFiles.add(files[i]);
		Utils.log("List of files at server are:");
		String output = "";
		for (String file : serverFiles)
			output += file + ",";
		Utils.log(output);
	}

}
