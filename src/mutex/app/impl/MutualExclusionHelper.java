package mutex.app.impl;

import java.io.PrintWriter;
import java.sql.Timestamp;

import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class MutualExclusionHelper {

	public static void sendRequestToProcess(Timestamp ownerTimestamp, int ownerProcessnum, int receivingProcessNum,
			String fileName, PrintWriter[] writer) {
		Utils.log("$$$-->Sending REQUEST to Process:" + receivingProcessNum + ",Timestamp:" + ownerTimestamp + " ,File:"
				+ fileName);
		int x = mapProcessNumToWriterIndex(ownerProcessnum, receivingProcessNum);
		writer[x].println(Config.REQUEST + "," + ownerTimestamp + "," + ownerProcessnum + "," + fileName);
	}

	public static void sendReplyToProcess(int receivingProcessNum, PrintWriter[] writer, int ownerProcessnum) {
		Utils.log("Sending REPLY to Process:" + receivingProcessNum);
		int x = mapProcessNumToWriterIndex(ownerProcessnum, receivingProcessNum);
		writer[x].println(Config.REPLY + "," + receivingProcessNum);
	}

	public static boolean evaluateDeferCondition(boolean requestedCSFlag, Timestamp senderTimestamp,
			Timestamp ownerTimestamp, int senderProcessNum, int ownerProcessNum, String senderFileName,
			String myFileName) {
		boolean defer = false;
		int comparisionOutcome = Utils.compareTimestamp(senderTimestamp, ownerTimestamp);
		boolean sameFile = checkFileSame(myFileName, senderFileName);
		if (requestedCSFlag && sameFile) {
			if (comparisionOutcome == 1) {
				defer = true;
			} else if (comparisionOutcome == 0) {
				if (senderProcessNum > ownerProcessNum) {
					defer = true;
				}
			}
		}
		return defer;
	}

	private static int mapProcessNumToWriterIndex(int ownerProcess, int receivingProcessNum) {
		if (receivingProcessNum > ownerProcess)
			return receivingProcessNum - 2;
		else
			return receivingProcessNum - 1;

	}

	private static boolean checkFileSame(String ownerFileName, String senderFileName) {
		boolean result = false;
		if (ownerFileName != null && !ownerFileName.isEmpty()) {
			if (ownerFileName.equalsIgnoreCase(senderFileName)) {
				result = true;
			}
		}
		return result;

	}

	public static void assignChannelWriters(MutualExclusionImpl mutexImpl, PrintWriter w1, PrintWriter w2,
			PrintWriter w3, PrintWriter w4) {
		PrintWriter pw[] = mutexImpl.getWriterForChannel();
		pw[0] = w1;
		pw[1] = w2;
		pw[2] = w3;
		pw[3] = w4;
		mutexImpl.setWriterForChannel(pw);
	}
}
