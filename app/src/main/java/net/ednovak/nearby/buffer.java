package net.ednovak.nearby;

public class buffer {
	public String session;
	public String message = "";
	public long start;
	public String sender;
	
	public buffer(String nSender, long nStart, String nSession){
		sender = nSender;
		start = nStart;
		session = nSession;
	}
	
	public void append(String m){
		message = message + m;
	}
}
