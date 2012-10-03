package net.ednovak.nearby;

import java.math.BigInteger;


public class shareSingleton {
	private static shareSingleton instance = null;
	public double lon;
	public double lat;
	public String rec;
	public xmppService serv;
	public int bits;
	public int method;
	public long start; // Holds the time the protocol was initiated by Alice dawg
	int session; // Holds the session number! (maybe this is unnecessary)
	public Paillier pKey;
	public boolean foundLon;
	
	// Alice uses these values to store whether or not the longitude / latitude matches
	// This allows us to wait until stage 4 to respond to user
	public boolean longitude = false;
	
	// This is the final key used by Alice and Bob to privately share the location info
	public BigInteger[] last;
	
	
	
	protected shareSingleton(){
		// Only to defeat instantiation
	}
	
	public static shareSingleton getInstance(){
		if (instance == null){
			instance = new shareSingleton();
		}
		return instance;
	}

}
