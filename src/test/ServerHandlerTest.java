package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import mutex.app.utils.Utils;

public class ServerHandlerTest implements Runnable {
	String name;
	Socket s;
	BufferedReader reader;
	PrintWriter writer;

	public ServerHandlerTest(String name, Socket s) {
		super();
		this.s = s;
		this.name = name;
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
		System.out.println("Running a new thread for " + this.name);
		String message;
		String sCurrentLine;
		BufferedReader filereader;

		try {
			while ((message = reader.readLine()) != null) {
				String tokens[] = message.split(",");
				String operation = tokens[0];
				String file = tokens[1];
				Utils.log("Operation:" + operation + " File:" + file);
				String lastLine = "";
				if (operation.equalsIgnoreCase("read")) {
					filereader = new BufferedReader(new FileReader("/home/eng/a/axn180041/mutex/s1/file1.txt"));
					while ((sCurrentLine = filereader.readLine()) != null) {
						Utils.log(sCurrentLine);
						lastLine = sCurrentLine;
					}
					Utils.log("Lastline:-->" + lastLine);
					Utils.log("From server: Sending the reply");
					writer.println("READ:-->" + lastLine);
				} else {
					String clientdata = tokens[2];
					Utils.log("Data to write: " + clientdata);
					File f = new File("/home/eng/a/axn180041/mutex/s1/file1.txt");
					FileWriter fw = new FileWriter(f, true);
					BufferedWriter filewriter = new BufferedWriter(fw);
					filewriter.write(clientdata + "\n");
					filewriter.close();
					fw.close();
					Utils.log("Finished writing data to file");
					Utils.log("From server: Sending the reply");
					writer.println("WROTE:-->" + clientdata);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
