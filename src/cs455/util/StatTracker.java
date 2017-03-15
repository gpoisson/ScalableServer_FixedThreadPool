package cs455.util;

// Simple collection of integers that gets passed around the system and adjusted according to data flow
public class StatTracker {
	
	private int reads;
	private int writes;
	private int hashes;
	private int connections;
	private long time;
	
	public StatTracker() {
		reads = 0;
		writes = 0;
		hashes = 0;
		connections = 0;
	}
	
	public void incrementReads() {
		reads++;
	}
	
	public void incrementWrites() {
		writes++;
	}
	
	public void incrementHashes() {
		hashes++;
	}
	
	public void incrementConnections() {
		connections++;
	}
	
	public void resetRW() {
		reads = 0;
		writes = 0;
	}
	
	public void setTime(long time){
		this.time = time;
	}
	
	public long getTime(){
		return this.time;
	}
	
	public void decrementConnections() {
		connections--;
	}
	
	public int getReadCount() {
		return reads;
	}
	
	public int getWriteCount() {
		return writes;
	}
	
	public int getHashCount() {
		return hashes;
	}
	
	public int getThroughput() {
		return (reads + writes) / 2;
	}
	
	public int getConnections() {
		return connections;
	}
}
