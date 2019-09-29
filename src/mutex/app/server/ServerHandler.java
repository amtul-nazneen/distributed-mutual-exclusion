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

import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class ServerHandler implements Runnable {
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;
	String serverFolder;

	public ServerHandler(Socket socket, String serverName) {
		super();
		this.socket = socket;
		this.serverFolder = serverName;
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

				if (operation.equalsIgnoreCase(Config.READ)) {
					String file = tokens[1];
					Utils.log("Operation:" + operation + " File:" + file);
					String lastLine = "";
					String accessFile = Config.FOLDER_PATH + serverFolder + "/" + file + Config.FILE_EXT;
					filereader = new BufferedReader(new FileReader(accessFile));
					while ((sCurrentLine = filereader.readLine()) != null) {
						Utils.log(sCurrentLine);
						lastLine = sCurrentLine;
					}
					Utils.log("Lastline:-->" + lastLine);
					Utils.log("From server: Sending the reply");
					writer.println("READ:-->" + lastLine);
				} else if (operation.equalsIgnoreCase(Config.WRITE)) {
					String file = tokens[1];
					Utils.log("Operation:" + operation + " File:" + file);
					String accessFile = Config.FOLDER_PATH + serverFolder + "/" + file + Config.FILE_EXT;
					String clientdata = tokens[2];
					Utils.log("Data to write: " + clientdata);
					File f = new File(accessFile);
					FileWriter fw = new FileWriter(f, true);
					BufferedWriter filewriter = new BufferedWriter(fw);
					filewriter.write(clientdata + Config.EOL);
					filewriter.close();
					fw.close();
					Utils.log("Finished writing data to file");
					Utils.log("From server: Sending the reply");
					writer.println("WROTE:-->" + clientdata);
				} else if (operation.equalsIgnoreCase(Config.ENQUIRE)) {
					String processnum = tokens[1];
					Utils.log("Received enquire from the process:" + processnum);
					String files = Config.SERVER_FILES;
					// TODO
					Utils.log("From server: Sending ENQUIRE result");
					writer.println(files);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
