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
	String name;
	Socket s;
	BufferedReader reader;
	PrintWriter writer;
	String server;

	public ServerHandler(String name, Socket s, String server) {
		super();
		this.s = s;
		this.name = name;
		this.server = server;
		try {
			InputStreamReader iReader = new InputStreamReader(s.getInputStream());
			reader = new BufferedReader(iReader);
			writer = new PrintWriter(s.getOutputStream(), true);
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

				if (operation.equalsIgnoreCase("read")) {
					String file = tokens[1];
					Utils.log("Operation:" + operation + " File:" + file);
					String lastLine = "";
					String accessFile = Config.FOLDER_PATH + server + "/" + file + Config.FILE_EXT;
					filereader = new BufferedReader(new FileReader(accessFile));
					while ((sCurrentLine = filereader.readLine()) != null) {
						Utils.log(sCurrentLine);
						lastLine = sCurrentLine;
					}
					Utils.log("Lastline:-->" + lastLine);
					Utils.log("From server: Sending the reply");
					writer.println("READ:-->" + lastLine);
				} else if (operation.equalsIgnoreCase("write")) {
					String file = tokens[1];
					Utils.log("Operation:" + operation + " File:" + file);
					String accessFile = Config.FOLDER_PATH + server + "/" + file + Config.FILE_EXT;
					String clientdata = tokens[2];
					Utils.log("Data to write: " + clientdata);
					File f = new File(accessFile);
					FileWriter fw = new FileWriter(f, true);
					BufferedWriter filewriter = new BufferedWriter(fw);
					filewriter.write(clientdata + "\n");
					filewriter.close();
					fw.close();
					Utils.log("Finished writing data to file");
					Utils.log("From server: Sending the reply");
					writer.println("WROTE:-->" + clientdata);
				} else if (operation.equalsIgnoreCase("enquire")) {
					String processnum = tokens[1];
					Utils.log("Received enquire from the process:" + processnum);
					String files = "file1.txt,file2.txt,file3.txt,file4.txt";
					Utils.log("From server: Sending the reply");
					writer.println("ENQUIRE result:-->" + files);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
