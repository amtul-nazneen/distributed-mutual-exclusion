package mutex.app.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import mutex.app.utils.Config;
import mutex.app.utils.Utils;

public class MutualExclusionImpl {
	public boolean myRequestCSFlag;
	public int myPendingReplyCount;
	public int highestSequenceNum;
	public int mySequenceNum;
	public int myProcessNum;
	public PrintWriter[] writerForChannel;
	// public int myChannelCount = Config.PROCESS_CHANNELS;
	// public boolean[] myDeferredReplyRepo;
	public ArrayList<DeferredReply> myDeferredReplies;
	public ArrayList<Integer> rcOptimize;

	public MutualExclusionImpl(int processnum, int sequenceNum) {
		this.mySequenceNum = sequenceNum;
		this.myProcessNum = processnum;
		init();
	}

	private void init() {
		// myRequestCSFlag = false;
		myPendingReplyCount = Config.PROCESS_CHANNELS;
		highestSequenceNum = 0;
		writerForChannel = new PrintWriter[Config.PROCESS_CHANNELS];
		// myDeferredReplyRepo = new boolean[myChannelCount];
		myDeferredReplies = new ArrayList<DeferredReply>();
		rcOptimize = new ArrayList<Integer>();
	}

	public boolean myCSRequestBegin() {
		Utils.log("Entering begin, Process:" + myProcessNum, false);
		myRequestCSFlag = true;
		mySequenceNum = highestSequenceNum + 1;
		// myPendingReplyCount = Config.PROCESS_CHANNELS;
		// if (rcOptimize == null || rcOptimize.isEmpty())
		{
			Utils.log("Optimized requests list is empty");
			int total = Config.PROCESS_CHANNELS + 1;
			for (int i = 1; i <= total; i++) {
				if (i != myProcessNum) {
					MutualExclusionHelper.sendRequestToProcess(mySequenceNum, myProcessNum, i, writerForChannel);
				}
			}
			Utils.log("Sent the requests to all processes, waiting for replies");
		} // else if (!rcOptimize.isEmpty())
		{
			/*
			 * Utils.log("Optimized requests list has size: " + rcOptimize.size()); String
			 * reqs = ""; for (Integer i : rcOptimize) { reqs = String.valueOf(i) + ",";
			 * MutualExclusionHelper.requestTo(mySequenceNum, myProcessNum, i,
			 * writerForChannel); } Utils.log("Sent the requests to " + reqs +
			 * ", waiting for replies");
			 */
		}

		while (myPendingReplyCount > 0) {
			try {
				Thread.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Utils.log("Exiting begin, Process:" + myProcessNum, false);
		return true;

	}

	public void myCSRequestEnd() {
		Utils.log("Entering end, Process:" + myProcessNum, false);
		myRequestCSFlag = false;

		Utils.log("Deferred Replies total: " + myDeferredReplies.size());
		Utils.log("Deferred Replies: Before sorting");
		for (DeferredReply dr : myDeferredReplies)
			Utils.log(dr.toString());

		Utils.log("Deferred Replies: After sorting");
		Collections.sort(myDeferredReplies, DeferredReply.drcomp);
		for (DeferredReply dr : myDeferredReplies)
			Utils.log(dr.toString());

		Utils.log("Deferred Replies that are to be sent");
		for (DeferredReply dr : myDeferredReplies) {
			Utils.log("Reply to be sent to process:" + dr.getProcessNum());
			MutualExclusionHelper.sendReplyToProcess(dr.getProcessNum(), writerForChannel, myProcessNum);// replyTo(dr.getProcessNum());
		}
		/*
		 * for (int i = 0; i < channelCount; i++) { if (replyDeferred[i]) {
		 * replyDeferred[i] = false; if (i < (processnum - 1)) replyTo(i + 1); else
		 * replyTo(i + 2); } }
		 */
		myDeferredReplies.clear();
		Utils.log("Clearing Deferred Replies, size now:" + myDeferredReplies.size());
		Utils.log("Done end(), Process:" + myProcessNum);
	}

	public void myReceivedRequest(int senderSeqNum, int senderProcessNum) {
		Utils.log("$$$-->Received REQUEST from Process:" + senderProcessNum + ",Seqnum:" + senderSeqNum);
		// boolean deferOutcome = false;
		if (senderSeqNum > highestSequenceNum)
			highestSequenceNum = senderSeqNum;

		// highestSequenceNum = Math.max(highestSequenceNum, senderSeqNum);
		Utils.log("HighestSeqNum in my process is now: " + highestSequenceNum);
		// toDefer = requestCSFlag
		// && ((senderSeqNum > mySequenceNum) || (senderSeqNum == mySequenceNum &&
		// senderProcessNum > processnum));
		boolean deferOutcome = MutualExclusionHelper.evaluateDeferCondition(myRequestCSFlag, senderSeqNum,
				mySequenceNum, senderProcessNum, myProcessNum);
		if (deferOutcome) {
			Utils.log("$$$-->DEFERRED sending message to Process:" + senderProcessNum);
			// if (senderProcessNum > myProcessNum)
			{
				// myDeferredReplyRepo[senderProcessNum - 2] = true;
				myDeferredReplies.add(new DeferredReply(true, senderProcessNum, senderSeqNum));
			}
			// else
			{
				// myDeferredReplyRepo[senderProcessNum - 1] = true;
				// myDeferredReplies.add(new DeferredReply(true, senderProcessNum,
				// senderSeqNum));
			}
		} else {
			MutualExclusionHelper.sendReplyToProcess(senderProcessNum, writerForChannel, myProcessNum);
		}
		Utils.log("Exiting ReceiveRequest", false);
	}

	public void myReceivedReply() {
		int curr = myPendingReplyCount - 1;
		if (curr > 0)
			myPendingReplyCount = curr;
		else
			myPendingReplyCount = 0;
		// pendingReplyCount = Math.max((pendingReplyCount - 1), 0);
		Utils.log("Outstanding replies:" + myPendingReplyCount);
	}

}
