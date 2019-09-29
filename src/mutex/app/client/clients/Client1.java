package mutex.app.client.clients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;

import mutex.app.client.ClientHandler;
import mutex.app.impl.MutualExclusionHelper;
import mutex.app.impl.MutualExclusionImpl;
import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class Client1 {
	MutualExclusionImpl myMutexImpl;
	int processnum = 1;
	int counter = 0;

	Socket server1 = null, server2 = null, server3 = null;
	Socket s2, s3, s4, s5;
	ServerSocket ss2, ss3, ss4, ss5;

	PrintWriter w2, w3, w4, w5;
	BufferedReader r2, r3, r4, r5;
	PrintWriter writeToServer1, writeToServer2, writeToServer3;
	BufferedReader readFromServer1, readFromServer2, readFromServer3;

	String[] serverFiles;

	public void startClient1() throws Exception {
		try {
			connectToServer();
			connectToOtherClients();
			createServerIOStream();
			createChannelIOStream();
			createMutexImplementor();
			startChannelThreads();

			while (counter < Config.CLIENT1_CSLIMIT) {
				try {
					requestForCSaccess();
					counter++;
					Thread.sleep((long) (Math.random() * 1000));
				} catch (Exception e) {
					Utils.log(e.getMessage());
				}
			}
		} catch (Exception e) {
			Utils.log(e.getMessage());
		}
	}

	public void requestForCSaccess() throws Exception {
		int attempt = counter + 1;
		Timestamp myRequestTime = Utils.getTimestamp();
		Utils.log("Begin CS_Access: " + attempt + " Timestamp: " + "[" + myRequestTime + "]");
		myMutexImpl.myCSRequestBegin(myRequestTime, "file1");
		executeCriticalSection(processnum, counter);
		myMutexImpl.myCSRequestEnd();
		Utils.log("End CS_Access: " + attempt + " Timestamp: " + "[" + Utils.getTimestamp() + "]");
	}

	private void executeCriticalSection(int processnum, int counter) throws Exception {
		int attempt = counter + 1;
		Utils.log("***>> Starting CS_Access: " + attempt);
		try {
			writeToServer();
			Thread.sleep(Config.CLIENT1_CSEXEC);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Utils.log("***>> Completed CS_Access: " + attempt);
	}

	private void readFromServer() throws Exception {
		writeToServer1.println(Config.READ + "," + "file1");
		String reply;
		boolean gotReply = false;
		while (!gotReply) {
			reply = readFromServer1.readLine();
			if (reply != null) {
				Utils.log("Received reply:-->" + reply);
				gotReply = true;
			}
		}
	}

	private void writeToServer() throws Exception {
		writeToServer1.println(Config.WRITE + "," + "file1" + "," + Config.WRITE_MESSAGE + processnum + " at "
				+ myMutexImpl.getMyRequestTimestamp());
		String reply;
		boolean gotReply = false;
		while (!gotReply) {
			reply = readFromServer1.readLine();
			if (reply != null) {
				gotReply = true;
			}
		}
	}

	private void enquireToServer() throws Exception {
		writeToServer1.println(Config.ENQUIRE + "," + processnum);
		boolean gotReply = false;
		String reply = "";
		while (!gotReply) {
			reply = readFromServer1.readLine();
			if (reply != null) {
				Utils.log("Received reply-->:" + reply);
				gotReply = true;
			}
		}
		Utils.log("Saving the list of available server files");
		serverFiles = new String[3];
		serverFiles = reply.split(",");
		for (int i = 0; i < serverFiles.length; i++)
			Utils.log(serverFiles[i]);
	}

	private void connectToServer() throws Exception {
		server1 = new Socket(Config.SERVER1_HOST, Config.SERVER_PORT);
		server2 = new Socket(Config.SERVER2_HOST, Config.SERVER_PORT);
		server3 = new Socket(Config.SERVER3_HOST, Config.SERVER_PORT);
	}

	private void connectToOtherClients() throws Exception {
		ss2 = new ServerSocket(Config.CLIENT2_PORT);
		ss3 = new ServerSocket(Config.CLIENT3_PORT);
		ss4 = new ServerSocket(Config.CLIENT4_PORT);
		ss5 = new ServerSocket(Config.CLIENT5_PORT);

		s2 = ss2.accept();
		s3 = ss3.accept();
		s4 = ss4.accept();
		s5 = ss5.accept();
	}

	private void createServerIOStream() throws Exception {
		writeToServer1 = new PrintWriter(server1.getOutputStream(), true);
		readFromServer1 = new BufferedReader(new InputStreamReader(server1.getInputStream()));
		writeToServer2 = new PrintWriter(server2.getOutputStream(), true);
		readFromServer2 = new BufferedReader(new InputStreamReader(server2.getInputStream()));
		writeToServer3 = new PrintWriter(server3.getOutputStream(), true);
		readFromServer3 = new BufferedReader(new InputStreamReader(server3.getInputStream()));
	}

	private void createChannelIOStream() throws Exception {
		w2 = new PrintWriter(s2.getOutputStream(), true);
		r2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
		w3 = new PrintWriter(s3.getOutputStream(), true);
		r3 = new BufferedReader(new InputStreamReader(s3.getInputStream()));
		w4 = new PrintWriter(s4.getOutputStream(), true);
		r4 = new BufferedReader(new InputStreamReader(s4.getInputStream()));
		w5 = new PrintWriter(s5.getOutputStream(), true);
		r5 = new BufferedReader(new InputStreamReader(s5.getInputStream()));
	}

	private void createMutexImplementor() {
		myMutexImpl = new MutualExclusionImpl(processnum);
		MutualExclusionHelper.assignChannelWriters(myMutexImpl, w2, w3, w4, w5);

	}

	private void startChannelThreads() {
		ClientHandler css2 = new ClientHandler(s2, myMutexImpl);
		ClientHandler css3 = new ClientHandler(s3, myMutexImpl);
		ClientHandler css4 = new ClientHandler(s4, myMutexImpl);
		ClientHandler css5 = new ClientHandler(s5, myMutexImpl);
		Thread t2 = new Thread(css2);
		Thread t3 = new Thread(css3);
		Thread t4 = new Thread(css4);
		Thread t5 = new Thread(css5);
		t2.start();
		t3.start();
		t4.start();
		t5.start();
	}

}
