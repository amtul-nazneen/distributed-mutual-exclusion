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
import mutex.app.utils.Constants;
import mutex.app.utils.Utils;

public class Client4 {

	MutualExclusionImpl myMutexImpl;
	int processnum = 4;
	int counter = 0;

	Socket server1 = null, server2 = null, server3 = null;
	Socket s2, s1, s3, s5;
	ServerSocket ss5;

	PrintWriter w1, w2, w3, w5;
	BufferedReader r1, r2, r3, r5;
	PrintWriter writeToServer1, writeToServer2, writeToServer3;
	BufferedReader readFromServer1, readFromServer2, readFromServer3;

	String[] serverFiles;

	public void startClient4() throws Exception {
		try {
			connectToServer();
			connectToOtherClients();
			createServerIOStream();
			createChannelIOStream();
			createMutexImplementor();
			startChannelThreads();

			while (counter < Constants.CLIENT4_CSLIMIT) {
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
			server1.close();
			server2.close();
			server3.close();
		}
	}

	public void requestForCSaccess() throws Exception {
		int attempt = counter + 1;
		Timestamp myRequestTime = Utils.getTimestamp();
		Utils.log("Begin CS_Access: " + attempt + " Timestamp: " + "[" + myRequestTime + "]");
		myMutexImpl.myCSRequestBegin(myRequestTime, "file2");
		executeCriticalSection(processnum, counter);
		myMutexImpl.myCSRequestEnd();
		Utils.log("End CS_Access: " + attempt + " Timestamp: " + "[" + Utils.getTimestamp() + "]");
	}

	private void executeCriticalSection(int processnum, int counter) throws Exception {
		int attempt = counter + 1;
		Utils.log("***>> Starting CS_Access: " + attempt);
		try {
			readFromServer();
			Thread.sleep(Constants.CLIENT4_CSEXEC);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Utils.log("***>> Completed CS_Access: " + attempt);
	}

	private void readFromServer() throws Exception {
		Utils.log("Reading from server");
		writeToServer1.println(Constants.READ + "," + "file2");
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

	private void writeToServer() throws Exception {
		writeToServer1.println(Constants.WRITE + "," + "file2" + "," + Constants.WRITE_MESSAGE + processnum + " at "
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
		Utils.log("Enquiring from server");
		writeToServer2.println(Constants.ENQUIRE + "," + processnum);
		Utils.log("Sent the enquire, waiting for reply");
		boolean gotReply = false;
		String reply = "";
		while (!gotReply) {
			reply = readFromServer2.readLine();
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
		server1 = new Socket(Constants.SERVER1_HOST, Constants.SERVER_PORT);
		server2 = new Socket(Constants.SERVER2_HOST, Constants.SERVER_PORT);
		server3 = new Socket(Constants.SERVER3_HOST, Constants.SERVER_PORT);

	}

	private void connectToOtherClients() throws Exception {
		s1 = new Socket(Constants.DC_PROC1, Constants.CLIENT4_PORT);
		s2 = new Socket(Constants.DC_PROC2, Constants.CLIENT4_PORT);
		s3 = new Socket(Constants.DC_PROC3, Constants.CLIENT4_PORT);
		ss5 = new ServerSocket(Constants.CLIENT5_PORT);
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
		w1 = new PrintWriter(s1.getOutputStream(), true);
		r1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
		w2 = new PrintWriter(s2.getOutputStream(), true);
		r2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
		w3 = new PrintWriter(s3.getOutputStream(), true);
		r3 = new BufferedReader(new InputStreamReader(s3.getInputStream()));
		w5 = new PrintWriter(s5.getOutputStream(), true);
		r5 = new BufferedReader(new InputStreamReader(s5.getInputStream()));
	}

	private void createMutexImplementor() {
		myMutexImpl = new MutualExclusionImpl(processnum);
		MutualExclusionHelper.assignChannelWriters(myMutexImpl, w1, w2, w3, w5);
	}

	private void startChannelThreads() {
		ClientHandler css1 = new ClientHandler(s1, myMutexImpl);
		ClientHandler css2 = new ClientHandler(s2, myMutexImpl);
		ClientHandler css3 = new ClientHandler(s3, myMutexImpl);
		ClientHandler css5 = new ClientHandler(s5, myMutexImpl);
		Thread t1 = new Thread(css1);
		Thread t2 = new Thread(css2);
		Thread t3 = new Thread(css3);
		Thread t5 = new Thread(css5);
		t1.start();
		t2.start();
		t3.start();
		t5.start();
	}

}
