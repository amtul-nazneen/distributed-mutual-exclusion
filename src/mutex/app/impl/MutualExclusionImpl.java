package mutex.app.impl;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

import mutex.app.utils.Constants;
import mutex.app.utils.Utils;

public class MutualExclusionImpl {
	private boolean myRequestCSFlag;
	private int myPendingReplyCount;
	private String myFileName;
	private int myProcessNum;
	private Timestamp myRequestTimestamp;
	private PrintWriter[] writerForChannel;
	private ArrayList<DeferredReply> myDeferredReplies;
	//private ArrayList<Integer> rcOptimize;

	public MutualExclusionImpl(int processnum) {
		this.myProcessNum = processnum;
		init();
	}

	private void init() {
		myPendingReplyCount = Constants.PROCESS_CHANNELS;
		myRequestTimestamp = null;
		writerForChannel = new PrintWriter[Constants.PROCESS_CHANNELS];
		myDeferredReplies = new ArrayList<DeferredReply>();
	//	rcOptimize = new ArrayList<Integer>();
		myFileName = "";
	}

	public boolean myCSRequestBegin(Timestamp time, String fileName) {
		myRequestCSFlag = true;
		myRequestTimestamp = time;
		myFileName = fileName;
		myPendingReplyCount = Constants.PROCESS_CHANNELS;
		{
			// Utils.log("Optimized requests list is empty");
			int total = Constants.PROCESS_CHANNELS + 1;
			for (int i = 1; i <= total; i++) {
				if (i != myProcessNum) {

					MutualExclusionHelper.sendRequestToProcess(myRequestTimestamp, myProcessNum, i, myFileName,
							writerForChannel);
				}
			}

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
		return true;

	}

	public void myCSRequestEnd() {
		myRequestCSFlag = false;
		myFileName = "";

		Collections.sort(myDeferredReplies, DeferredReply.DREP_COMP);
		Utils.log("Total Deferred Replies:" + myDeferredReplies.size());
		for (DeferredReply dr : myDeferredReplies) {
			MutualExclusionHelper.sendReplyToProcess(dr.getProcessNum(), writerForChannel, myProcessNum);// replyTo(dr.getProcessNum());
		}
		myDeferredReplies.clear();
	}

	public void myReceivedRequest(Timestamp senderTimestamp, int senderProcessNum, String senderFileName) {
		Utils.log("-->Received REQUEST from Process:" + senderProcessNum + " ,SenderTimestamp:" + senderTimestamp
				+ " ,File:" + senderFileName);

		boolean deferOutcome = MutualExclusionHelper.evaluateDeferCondition(myRequestCSFlag, senderTimestamp,
				myRequestTimestamp, senderProcessNum, myProcessNum, senderFileName, myFileName);
		if (deferOutcome) {
			Utils.log("-->DEFERRED sending message to Process:" + senderProcessNum);
			myDeferredReplies.add(new DeferredReply(true, senderProcessNum, senderTimestamp));

		} else {
			MutualExclusionHelper.sendReplyToProcess(senderProcessNum, writerForChannel, myProcessNum);
		}
	}

	public void myReceivedReply() {
		int curr = myPendingReplyCount - 1;
		if (curr > 0)
			myPendingReplyCount = curr;
		else
			myPendingReplyCount = 0;
	}

	public String getFileCSAccess() {
		return myFileName;
	}

	public void setFileCSAccess(String fileCSAccess) {
		this.myFileName = fileCSAccess;
	}

	public String getMyRequestTimestamp() {
		return "[" + myRequestTimestamp + "]";
	}

	public PrintWriter[] getWriterForChannel() {
		return writerForChannel;
	}

	public void setWriterForChannel(PrintWriter[] writerForChannel) {
		this.writerForChannel = writerForChannel;
	}

}
