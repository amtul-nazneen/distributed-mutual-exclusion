package mutex.app.client.clients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mutex.app.client.ClientHandler;
import mutex.app.impl.MutualExclusionHelper;
import mutex.app.impl.MutualExclusionImpl;
import mutex.app.utils.Constants;
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

	ArrayList<String> serverFileList;
	ArrayList<String> serverList;
	ArrayList<String> taskList;
	HashMap<String, PrintWriter> serverToWriter;
	HashMap<String, BufferedReader> serverToReader;

	String SERVER = "";
	String FILE = "";
	String TASK = "";

	public void startClient1() throws Exception {
		try {
			connectToServer();
			connectToOtherClients();
			createServerIOStream();
			createChannelIOStream();
			createMutexImplementor();
			startChannelThreads();
			init();
			while (counter < Constants.CLIENT1_CSLIMIT) {
				try {
					setRandomRequestParams();
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
		myMutexImpl.myCSRequestBegin(myRequestTime, FILE);
		executeCriticalSection(processnum, counter);
		myMutexImpl.myCSRequestEnd();
		Utils.log("End CS_Access: " + attempt + " Timestamp: " + "[" + Utils.getTimestamp() + "]");
	}

	private void executeCriticalSection(int processnum, int counter) throws Exception {
		int attempt = counter + 1;
		Utils.log("======= Starting  CS_Access: [[[[[[[[[" + attempt + "]]]]]]]]] ===========");
		try {
			if (Constants.READ.equalsIgnoreCase(TASK))
				readFromServer();
			else if (Constants.WRITE.equalsIgnoreCase(TASK))
				writeToAllServers();
			Thread.sleep(Constants.CLIENT1_CSEXEC);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Utils.log("======= Completed CS_Access: [[[[[[[[[" + attempt + "]]]]]]]]] ===========");
	}

	private void readFromServer() throws Exception {
		PrintWriter writeToServer = serverToWriter.get(SERVER);
		BufferedReader readFromServer = serverToReader.get(SERVER);
		writeToServer.println(Constants.READ + "," + FILE);
		String reply;
		boolean gotReply = false;
		while (!gotReply) {
			reply = readFromServer.readLine();
			if (reply != null) {
				Utils.log("Read from server:-->" + "{ " + reply + " } ");
				gotReply = true;
			}
		}
	}

	private void writeToAllServers() throws Exception {
		writeToServer1.println(Constants.WRITE + "," + FILE + "," + Constants.WRITE_MESSAGE + processnum + " at "
				+ myMutexImpl.getMyRequestTimestamp());
		writeToServer2.println(Constants.WRITE + "," + FILE + "," + Constants.WRITE_MESSAGE + processnum + " at "
				+ myMutexImpl.getMyRequestTimestamp());
		writeToServer3.println(Constants.WRITE + "," + FILE + "," + Constants.WRITE_MESSAGE + processnum + " at "
				+ myMutexImpl.getMyRequestTimestamp());
		String reply = null;
		boolean gotReply = false;
		while (!gotReply) {
			reply = readFromServer1.readLine();
			if (reply != null) {
				gotReply = true;
			}
		}
		Utils.log("Got reply from Server1:" + reply);

		gotReply = false;
		while (!gotReply) {
			reply = readFromServer2.readLine();
			if (reply != null) {
				gotReply = true;
			}
		}
		Utils.log("Got reply from Server2:" + reply);

		gotReply = false;
		while (!gotReply) {
			reply = readFromServer3.readLine();
			if (reply != null) {
				gotReply = true;
			}
		}
		Utils.log("Got reply from Server3:" + reply);
	}

	private void enquireToServer() throws Exception {
		writeToServer1.println(Constants.ENQUIRE + "," + processnum);
		boolean gotReply = false;
		String reply = "";
		while (!gotReply) {
			reply = readFromServer1.readLine();
			if (reply != null) {
				gotReply = true;
			}
		}
		Utils.log("Saving enquired server files");
		String temp[] = reply.split(",");
		serverFileList = new ArrayList<String>();
		for (int i = 0; i < temp.length; i++)
			serverFileList.add(temp[i]);
		Collections.sort(serverFileList);
	}

	private void connectToServer() throws Exception {
		server1 = new Socket(Constants.SERVER1_HOST, Constants.SERVER_PORT);
		server2 = new Socket(Constants.SERVER2_HOST, Constants.SERVER_PORT);
		server3 = new Socket(Constants.SERVER3_HOST, Constants.SERVER_PORT);
	}

	private void connectToOtherClients() throws Exception {
		ss2 = new ServerSocket(Constants.CLIENT2_PORT);
		ss3 = new ServerSocket(Constants.CLIENT3_PORT);
		ss4 = new ServerSocket(Constants.CLIENT4_PORT);
		ss5 = new ServerSocket(Constants.CLIENT5_PORT);

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

	private void init() throws Exception {
		serverList = new ArrayList<String>();
		serverList.add(Constants.SERVER_1);
		serverList.add(Constants.SERVER_2);
		serverList.add(Constants.SERVER_3);

		taskList = new ArrayList<String>();
		taskList.add(Constants.WRITE);
		taskList.add(Constants.READ);

		enquireToServer();

		serverToWriter = new HashMap<String, PrintWriter>();
		serverToWriter.put(Constants.SERVER_1, writeToServer1);
		serverToWriter.put(Constants.SERVER_2, writeToServer2);
		serverToWriter.put(Constants.SERVER_3, writeToServer3);

		serverToReader = new HashMap<String, BufferedReader>();
		serverToReader.put(Constants.SERVER_1, readFromServer1);
		serverToReader.put(Constants.SERVER_2, readFromServer2);
		serverToReader.put(Constants.SERVER_3, readFromServer3);
	}

	private void setRandomRequestParams() {
		/* For selecting file */
		int randomInt = (int) (30.0 * Math.random());
		if (randomInt <= 10 && randomInt >= 0)
			FILE = serverFileList.get(0);
		if (randomInt <= 20 && randomInt >= 11)
			FILE = serverFileList.get(1);
		if (randomInt <= 30 && randomInt >= 21)
			FILE = serverFileList.get(2);
		/* For selecting task */
		randomInt = (int) (20.0 * Math.random());
		if (randomInt <= 10 && randomInt >= 0)
			TASK = taskList.get(0);
		if (randomInt <= 20 && randomInt >= 11)
			TASK = taskList.get(1);
		if (Constants.READ.equalsIgnoreCase(TASK)) {
			/* For selecting server */
			randomInt = (int) (30.0 * Math.random());
			if (randomInt <= 10 && randomInt >= 0)
				SERVER = serverList.get(0);
			if (randomInt <= 20 && randomInt >= 11)
				SERVER = serverList.get(1);
			if (randomInt <= 30 && randomInt >= 21)
				SERVER = serverList.get(2);
		}
		if (Constants.READ.equalsIgnoreCase(TASK))
			Utils.log(" ********* Randomly Chosen, " + "TASK:" + TASK + " ," + Utils.getServerNameFromCode(SERVER)
					+ " ,FILE:" + FILE);
		else
			Utils.log(" ********* Randomly Chosen, " + "TASK:" + TASK + " ,FILE:" + FILE);
	}
}
