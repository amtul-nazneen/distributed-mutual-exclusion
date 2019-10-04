package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import mutex.app.utils.Constants;
import mutex.app.utils.Utils;

public class TestFiles {

	public static void main(String[] args) {
		ArrayList<String> files= new ArrayList<String>();
		files.add("file2.txt");
		files.add("file1.txt");
		files.add("file3.txt");
		Collections.sort(files);
		for(String s: files)
			System.out.println(s);
		Utils.logWithSeparator("blah");
	}
	
	/*private void writeToServer() throws Exception {
		writeToServer1.println(Constants.WRITE + "," + FILE + "," + Constants.WRITE_MESSAGE + processnum + " at "
				+ myMutexImpl.getMyRequestTimestamp());
		String reply;
		boolean gotReply = false;
		while (!gotReply) {
			reply = readFromServer1.readLine();
			if (reply != null) {
				gotReply = true;
			}
		}
	}*/
	
	public void readFile()
	{
		String files = "";
		File folder = new File("s1");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (i == 0) {
					files = listOfFiles[i].getName();
				}
				else
				{
					files=files+ ","+listOfFiles[i].getName();
				}
			}
		}
		System.out.println("Output: " + files);
	}
}

