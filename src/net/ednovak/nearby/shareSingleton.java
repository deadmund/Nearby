package net.ednovak.nearby;

import java.math.*;

public class shareSingleton {
	private static shareSingleton instance = null;
	public BigInteger g;
	public BigInteger lambda;
	public BigInteger n;
	public int pol;
	public double lon;
	public double lat;
	public String number;
	public xmppService serv;
	
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
