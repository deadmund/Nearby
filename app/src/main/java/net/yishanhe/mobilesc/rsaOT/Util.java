package net.yishanhe.mobilesc.rsaOT;


public class Util {


	public static int byteArrayToInt(byte[] b) 
	{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	public static byte[] intToByteArray(int a)
	{
	    return new byte[] {
	        (byte) ((a >>> 24) & 0xFF),
	        (byte) ((a >>> 16) & 0xFF),   
	        (byte) ((a >>> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	

	public static void xor(byte[] eHash, byte[] bytes) {
		int length;
		if(eHash.length<=bytes.length)
			length=eHash.length;
		else
			length=bytes.length;
		for (int i = 0; i < length; i++) {
			eHash[i] = (byte) (eHash[i] ^ bytes[i]);
		}

	}

	
	public static byte[] getByteArrayByLength(int len){
		int r = len%8;
		if(r==0){
			return new byte[len/8];
		}
		else{
			return new byte[len/8+1];
		}
	}

	
	public static boolean compareByteArrays(byte[] ba1, byte[] ba2){
		if(ba1.length!=ba2.length)
			return false;
		for(int i=0;i<ba1.length;i++){
			if(ba1[i]!=ba2[i])
				return false;
		}
		return true;
	}
	
	public static byte[] concatenate(byte[] ba1, byte[] ba2){
		byte[] res= new byte[ba1.length+ba2.length];
		System.arraycopy(ba1, 0, res, 0, ba1.length);
		System.arraycopy(ba2, 0, res, ba1.length, ba2.length);
		return res;
	}


	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
