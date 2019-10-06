package mutex.app.impl;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

import mutex.app.utils.Constants;
import mutex.app.utils.Utils;

/**
 * @author amtul.nazneen
 */

/**
 * Main class for implementing Ricart-Agrawala Algorithm
 */
public class MutualExclusionImpl {
	private boolean myRequestCSFlag;
	private int myPendingReplyCount;
	private String myFileName;
	private int myProcessNum;
	private Timestamp myRequestTimestamp;
	private PrintWriter[] writerForChannel;
	private ArrayList<DeferredReply> myDeferredReplies;
	private boolean requestagainF1[];
	private boolean myFirstCSBeginCompleted;
	private boolean updatingrequestagainF1;
	public boolean executingCSFlag;
	public boolean finishedCSFlag;

	public MutualExclusionImpl(int processnum) {
		this.myProcessNum = processnum;
		init();
	}

	/**
	 * Initialise helper variables to maintain the state
	 */
	private void init() {
		myPendingReplyCount = Constants.PROCESS_CHANNELS;
		myRequestTimestamp = null;
		writerForChannel = new PrintWriter[Constants.PROCESS_CHANNELS];
		myDeferredReplies = new ArrayList<DeferredReply>();
		myFileName = "";
		requestagainF1 = new boolean[Constants.TOTAL_CLIENTS + 1];
		myFirstCSBeginCompleted = false;
		updatingrequestagainF1 = false;
		executingCSFlag = false;
		finishedCSFlag = false;
	}

	/**
	 * Method that's called when a client requests for CS If the CS is requested for
	 * the first time on a resource, the request is sent to all clients If the
	 * client has already accessed critical section once, then subsequently it sends
	 * requests only to the processes to which it replied during or after critical
	 * section
	 * 
	 * @param time
	 * @param fileName
	 * @return
	 */
	public boolean myCSRequestBegin(Timestamp time, String fileName) {
		myRequestCSFlag = true;
		myRequestTimestamp = time;
		myFileName = fileName;
		if (!myFirstCSBeginCompleted) {
			Utils.log("First CS, Sending request to all processes");
			myPendingReplyCount = Constants.PROCESS_CHANNELS;

			int total = Constants.PROCESS_CHANNELS + 1;
			for (int i = 1; i <= total; i++) {
				if (i != myProcessNum) {

					MutualExclusionHelper.sendRequestToProcess(myRequestTimestamp, myProcessNum, i, myFileName,
							writerForChannel);
				}
			}
			myFirstCSBeginCompleted = true;
		} else {
			String reqs = "";
			int count = 0;
			for (int i = 1; i <= 5; i++) {
				if (i != myProcessNum && requestagainF1[i]) {
					reqs = reqs + String.valueOf(i) + ", ";
					count++;
				}
			}
			myPendingReplyCount = count;
			if ((reqs != null) && (reqs.length() > 0)) {
				reqs = reqs.substring(0, reqs.length() - 1);
			}
			Utils.log("With optimization, sending Requests only to Process(es): " + reqs);
			Utils.log("Remaining Replies: " + count);
			for (int i = 1; i <= 5; i++) {
				if (i != myProcessNum && requestagainF1[i]) {
					MutualExclusionHelper.sendRequestToProcess(myRequestTimestamp, myProcessNum, i, myFileName,
							writerForChannel);
				}
			}
		}

		while (myPendingReplyCount > 0) {
			try {
				Thread.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		updatingrequestagainF1 = true;
		for (int i = 1; i <= 5; i++)
			requestagainF1[i] = false;
		updatingrequestagainF1 = false;
		Utils.log("Got all replies, setting 'requestAgainQueue' to false for all processes");
		return true;

	}

	/**
	 * Method that's called when the client critical section ends It sends out all
	 * the deferred replies and clears its queue
	 */
	public void myCSRequestEnd() {
		myRequestCSFlag = false;
		myFileName = "";

		Collections.sort(myDeferredReplies, DeferredReply.DREP_COMP);
		Utils.log("Total Deferred Replies:" + myDeferredReplies.size());
		for (DeferredReply dr : myDeferredReplies) {
			MutualExclusionHelper.sendReplyToProcess(dr.getProcessNum(), writerForChannel, myProcessNum);// replyTo(dr.getProcessNum());
			requestagainF1[dr.getProcessNum()] = true;
			// Utils.log("Added Process:" + dr.getProcessNum() + " to requestagain queue");
		}
		myDeferredReplies.clear();
	}

	/**
	 * Method that's called when the owning client receives a request
	 * 
	 * @param senderTimestamp
	 * @param senderProcessNum
	 * @param senderFileName
	 */
	public void myReceivedRequest(Timestamp senderTimestamp, int senderProcessNum, String senderFileName) {
		Utils.log("-->Received REQUEST from Process:" + senderProcessNum + " ,SenderTimestamp:" + senderTimestamp
				+ " ,File:" + senderFileName);
		while (updatingrequestagainF1) {
			try {
				Utils.log("Holding on..");
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean deferOutcome = MutualExclusionHelper.evaluateDeferCondition(myRequestCSFlag, senderTimestamp,
				myRequestTimestamp, senderProcessNum, myProcessNum, senderFileName, myFileName);
		if (deferOutcome) {
			Utils.log("-->DEFERRED sending message to Process:" + senderProcessNum);
			myDeferredReplies.add(new DeferredReply(true, senderProcessNum, senderTimestamp));

		} else {
			MutualExclusionHelper.sendReplyToProcess(senderProcessNum, writerForChannel, myProcessNum);
			// Utils.log("my cs request flag: " + myRequestCSFlag);
			// Utils.log("my cs exec Flag: " + executingCSFlag);
			if (executingCSFlag || finishedCSFlag) {
				requestagainF1[senderProcessNum] = true;
				// Utils.log("Added Process:" + senderProcessNum + " to request again queue");
			}
		}
	}

	/**
	 * Method that's called when the owning client receives a reply for its request
	 */
	public void myReceivedReply() {
		int curr = myPendingReplyCount - 1;
		if (curr > 0)
			myPendingReplyCount = curr;
		else
			myPendingReplyCount = 0;
		Utils.log("Remaining Replies: " + myPendingReplyCount);
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
