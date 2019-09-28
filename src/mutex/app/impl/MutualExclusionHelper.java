package mutex.app.impl;

import java.io.PrintWriter;

import mutex.app.utils.Utils;

public class MutualExclusionHelper {
	public static void sendRequestToProcess(int sequenceNum, int ownerProcessnum, int receivingProcessNum,
			PrintWriter[] writer) {
		Utils.log("$$$-->Sending REQUEST to Process:" + receivingProcessNum + ",Seqnum:" + sequenceNum);
		int x = mapProcessNumToWriterIndex(ownerProcessnum, receivingProcessNum);
		writer[x].println("REQUEST," + sequenceNum + "," + ownerProcessnum);
		/*
		 * if (i > processnum) { writer[i - 2].println("REQUEST," + seqNum + "," +
		 * processnum); } else { writer[i - 1].println("REQUEST," + seqNum + "," +
		 * processnum); }
		 */
	}

	public static void sendReplyToProcess(int receivingProcessNum, PrintWriter[] writer, int ownerProcessnum) {
		Utils.log("Sending REPLY to Process:" + receivingProcessNum);
		int x = mapProcessNumToWriterIndex(ownerProcessnum, receivingProcessNum);
		writer[x].println("REPLY," + receivingProcessNum);
		/*
		 * if (k > processnum) { writer[k - 2].println("REPLY," + k); } else { writer[k
		 * - 1].println("REPLY," + k); }
		 */
	}

	public static boolean evaluateDeferCondition(boolean requestedCSFlag, int senderSeqNum, int mySequenceNum,
			int senderProcessNum, int myProcessNum) {
		boolean defer = false;
		if (requestedCSFlag) {
			if (senderSeqNum > mySequenceNum) {
				defer = true;
			} else if (senderSeqNum == mySequenceNum) {
				if (senderProcessNum > myProcessNum) {
					defer = true;
				}
			}
		}
		return defer;
//		status = requestedCSFlag
//				&& ((senderSeqNum > mySequenceNum) || (senderSeqNum == mySequenceNum && senderProcessNum > processnum));
	}

	private static int mapProcessNumToWriterIndex(int ownerProcess, int receivingProcessNum) {
		if (receivingProcessNum > ownerProcess)
			return receivingProcessNum - 2;
		else
			return receivingProcessNum - 1;

	}

}
