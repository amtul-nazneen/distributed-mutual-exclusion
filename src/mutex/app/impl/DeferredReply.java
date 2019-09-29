package mutex.app.impl;

import java.sql.Timestamp;
import java.util.Comparator;

import mutex.app.utils.Utils;

public class DeferredReply implements Comparable<DeferredReply> {
	public boolean isDeferred;
	private int processNum;
	private Timestamp timestamp;

	public DeferredReply(boolean isDeferred, int processNum, Timestamp timestamp) {
		super();
		this.isDeferred = isDeferred;
		this.processNum = processNum;
		this.timestamp = timestamp;
	}

	public boolean isDeferred() {
		return isDeferred;
	}

	public void setDeferred(boolean isDeferred) {
		this.isDeferred = isDeferred;
	}

	public int getProcessNum() {
		return processNum;
	}

	public void setProcessNum(int processNum) {
		this.processNum = processNum;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int compareTo(DeferredReply o) {
		return this.processNum - o.getProcessNum();
	}

	public static final Comparator<DeferredReply> DREP_COMP = new Comparator<DeferredReply>() {

		@Override
		public int compare(DeferredReply o1, DeferredReply o2) {
			int c = Utils.compareTimestamp(o1.getTimestamp(), o2.getTimestamp());
			if (c == 0)
				c = o1.getProcessNum() - o2.getProcessNum();
			return c;
		}

	};

	@Override
	public String toString() {
		return "DeferredReply [isDeferred=" + isDeferred + ", processNum=" + processNum + ", timestamp=" + timestamp
				+ "]";
	}

}
