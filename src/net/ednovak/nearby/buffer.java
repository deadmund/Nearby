package net.ednovak.nearby;

public class buffer {
	public int session;
	public String message;
	public long start;
	public String sender;
	
	public buffer(String nSender, long nStart){
		sender = nSender;
		start = nStart;
	}
	
	public void append(String m){
		message = message + m;
	}
}
