package mutex.app.runner;

import java.io.PrintWriter;

import mutex.app.utils.Utils;

public class MutualExclusionImpl {
	public boolean bRequestingCS;
	public int outstandingReplies;
	public int highestSeqNum;
	public int seqNum;
	public int processnum;
	public PrintWriter[] w;
	public int channelCount = 4;
	public boolean[] replyDeferred;

	public MutualExclusionImpl(int processnum, int seqNum) {
		bRequestingCS = false;
		outstandingReplies = channelCount;
		highestSeqNum = 0;
		this.seqNum = seqNum;
		w = new PrintWriter[channelCount];
		this.processnum = processnum;
		replyDeferred = new boolean[channelCount];
	}

	public boolean invocation() {
		Utils.log("Entering Invocation, Process:" + processnum, false);
		bRequestingCS = true;
		seqNum = highestSeqNum + 1;
		outstandingReplies = channelCount;

		for (int i = 1; i <= channelCount + 1; i++) {
			if (i != processnum) {
				requestTo(seqNum, processnum, i);
			}
		}
		Utils.log("Sent the requests, waiting for replies");
		while (outstandingReplies > 0) {
			try {
				Thread.sleep(5);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Utils.log("Exiting Invocation, Process:" + processnum, false);
		return true;

	}

	public void requestTo(int seqNum, int processnum, int i) {
		Utils.log("$$$-->Sending REQUEST to Process:" + (((i))) + ",Seqnum:" + seqNum);
		if (i > processnum) {
			w[i - 2].println("REQUEST," + seqNum + "," + processnum);
		} else {
			w[i - 1].println("REQUEST," + seqNum + "," + processnum);
		}
	}

	public void releaseCS() {
		Utils.log("Entering ReleaseCS, Process:" + processnum, false);
		bRequestingCS = false;

		for (int i = 0; i < channelCount; i++) {
			if (replyDeferred[i]) {
				replyDeferred[i] = false;
				if (i < (processnum - 1))
					replyTo(i + 1);
				else
					replyTo(i + 2);
			}
		}
		Utils.log("Exiting ReleaseCS, Process:" + processnum, false);
		Utils.log("Done ReleaseCS, Process:" + processnum);
	}

	public void receiveRequest(int j, int k) {
		Utils.log("$$$-->Received REQUEST from Process:" + k + ",Seqnum:" + j);
		boolean bDefer = false;

		highestSeqNum = Math.max(highestSeqNum, j);
		Utils.log("HighestSeqNum now is: " + highestSeqNum);
		bDefer = bRequestingCS && ((j > seqNum) || (j == seqNum && k > processnum));
		if (bDefer) {
			Utils.log("$$$-->DEFERRED sending message to Process:" + k);
			if (k > processnum)
				replyDeferred[k - 2] = true;
			else
				replyDeferred[k - 1] = true;
		} else {
			replyTo(k);
		}
		Utils.log("Exiting ReceiveRequest", false);
	}

	public void receiveReply() {
		outstandingReplies = Math.max((outstandingReplies - 1), 0);
		Utils.log("Outstanding replies:" + outstandingReplies);
	}

	public void replyTo(int k) {
		Utils.log("Sending REPLY to Process:" + k);
		if (k > processnum) {
			w[k - 2].println("REPLY," + k);
		} else {
			w[k - 1].println("REPLY," + k);
		}
	}

}
