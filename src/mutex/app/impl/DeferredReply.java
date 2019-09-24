package mutex.app.impl;

import java.util.Comparator;

public class DeferredReply implements Comparable<DeferredReply> {
	public boolean isDeferred;
	public int seqNum;
	public int processNum;

	public DeferredReply(boolean isDeferred, int processNum, int seqNum) {
		super();
		this.isDeferred = isDeferred;
		this.processNum = processNum;
		this.seqNum = seqNum;
	}

	public boolean isDeferred() {
		return isDeferred;
	}

	public void setDeferred(boolean isDeferred) {
		this.isDeferred = isDeferred;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public int getProcessNum() {
		return processNum;
	}

	public void setProcessNum(int processNum) {
		this.processNum = processNum;
	}

	@Override
	public int compareTo(DeferredReply o) {
		return this.seqNum - o.getSeqNum();
	}

	public static final Comparator<DeferredReply> drcomp = new Comparator<DeferredReply>() {

		@Override
		public int compare(DeferredReply o1, DeferredReply o2) {
			int c = o1.getSeqNum() - o2.getSeqNum();
			if (c == 0)
				c = o1.getProcessNum() - o2.getProcessNum();
			return c;
		}

	};

	@Override
	public String toString() {
		return "DeferredReply [isDeferred=" + isDeferred + ", seqNum=" + seqNum + ", processNum=" + processNum + "]";
	}

}
