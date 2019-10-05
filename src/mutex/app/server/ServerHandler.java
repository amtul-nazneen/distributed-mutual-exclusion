package mutex.app.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import mutex.app.utils.Constants;
import mutex.app.utils.Utils;

public class ServerHandler implements Runnable {
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;
	String serverFolder;
	int clientId;

	public ServerHandler(Socket socket, String serverName, int clientId) {
		super();
		this.socket = socket;
		this.serverFolder = serverName;
		this.clientId = clientId;
		try {
			InputStreamReader iReader = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(iReader);
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String message;
		String sCurrentLine;
		BufferedReader filereader;

		try {
			while ((message = reader.readLine()) != null) {
				String tokens[] = message.split(",");
				String operation = tokens[0];

				if (operation.equalsIgnoreCase(Constants.READ)) {
					String file = tokens[1];
					Utils.log("Received from Process:" + clientId + " -- Operation:" + operation.toUpperCase()
							+ " ,File:" + file);
					String lastLine = "";
					String accessFile = Constants.FOLDER_PATH + serverFolder + "/" + file;
					filereader = new BufferedReader(new FileReader(accessFile));
					while ((sCurrentLine = filereader.readLine()) != null) {
						lastLine = sCurrentLine;
					}
					Utils.log("Sending Lastline of " + file + " to Process:" + clientId);
					writer.println("Lastline of " + file + " is: " + lastLine);
				} else if (operation.equalsIgnoreCase(Constants.WRITE)) {
					String file = tokens[1];
					Utils.log("Received from Process:" + clientId + " -- Operation:" + operation.toUpperCase()
							+ " File:" + file);
					String accessFile = Constants.FOLDER_PATH + serverFolder + "/" + file;
					String clientdata = tokens[2];
					Utils.log("Data to write in " + file + " from Process:" + clientId + "-- " + clientdata);
					File f = new File(accessFile);
					FileWriter fw = new FileWriter(f, true);
					BufferedWriter filewriter = new BufferedWriter(fw);
					filewriter.write(clientdata + Constants.EOL);
					filewriter.close();
					fw.close();
					Utils.log("For Process:" + clientId + ", finished Writing to File:" + file);
					writer.println("Finished writing to " + file + " :-->" + "{ " + clientdata + "} ");
				} else if (operation.equalsIgnoreCase(Constants.ENQUIRE)) {
					String processnum = tokens[1];
					Utils.log("Received ENQUIRE from Process:" + processnum);
					String files = null;
					File folder = new File(serverFolder);
					File[] listOfFiles = folder.listFiles();
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							if (i == 0) {
								files = listOfFiles[i].getName();
							} else {
								files = files + "," + listOfFiles[i].getName();
							}
						}
					}
					Utils.log("Sending ENQUIRE results to Process:" + processnum);
					writer.println(files);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
