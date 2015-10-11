package net.yishanhe.mobilesc.ot;

import org.spongycastle.math.ec.ECPoint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;

public class Util {

    public static String intToBitString(int a){
        return Integer.toBinaryString(a);
    }

    public static int bitStringToInt(String bs){
        return (int)Integer.valueOf(bs,2);
    }

	 /**
     * Convert an array of 4 bytes into an integer.
     *
     * @param  b The byte array to be converted
     */
	public static int byteArrayToInt(byte[] b) 
	{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	 /**
     * Convert an integer into a byte array of 4 bytes.
     *
     * @param  a The integer to be converted
     */
	public static byte[] intToByteArray(int a)
	{
	    return new byte[] {
	        (byte) ((a >>> 24) & 0xFF),
	        (byte) ((a >>> 16) & 0xFF),   
	        (byte) ((a >>> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	

	// xores two byte arrays of the same length, and store the result in the
	// first one. The two byte arrays are left aligned. If the second array is bigger than the first array,
	// then all bytes after bytes[eHash.length-1] are lost. 
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
	
	 /**
     * Get the ith bit in the selection string. Return true if the bit is set to 1, otherwise
     * return false
     *
     * @param  i index of bit to get (start from 0).
     */
	public static boolean getBit(int i, int leadingZeroes, byte[] str)  {

//        boolean test = (str[(i+leadingZeroes) >>> 3] & (1 << (7-((i+leadingZeroes) & 7)))) != 0;
//
//        int testA;
//        if (test){
//            testA = 1;
//        } else {
//            testA = 0;
//        }
//        BigInteger testBI = new BigInteger(str);
//        String testString = testBI.toString(2).;
//
//        if( Integer.parseInt(""+testString.charAt(i)) !=testA   ){
//            System.out.println("Wrong to use getBit.");
//        }
//
//        if(){
//
//        }

		return (str[(i+leadingZeroes) >>> 3] & (1 << (7-((i+leadingZeroes) & 7)))) != 0;
	    
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
	
	/**
	 * for a byte array that encodes a len-bit string, how many unused bits at the beginning of the array.
	 * @param len
	 * @return
	 */
	
	public static int getLeadingZeroes(int len){
		int r = len%8;
		
		if(r==0){
			return 0;
		}
		else{
			return 8-r;
		}
	}

    public static byte[] expandByteArrayForCT(byte[] src){
        byte[] des = new byte[src.length+1];
        des[0] = (byte)0x00;
        System.arraycopy(src,0,des,1,src.length);
        return des;
    }

    public static byte[] expandByteArray(byte[] src, int desLen){

        if(src.length>desLen){
            System.out.println("expandByteArray length mismatch");
        }

//        byte[] a = ByteBuffer.allocate(desLen).putInt(new BigInteger(src).intValue()).array();

        BigInteger srcb = new BigInteger(src);

        int srcLen = src.length;

        byte[] des = new byte[desLen];

        for (int i = 0; i < desLen; i++) {
            if(i< desLen-srcLen){
                if(srcb.signum() == -1) {
                    des[i] = (byte)0xFF;
//                    des[i] = (byte)0x00;
                } else {
                    des[i] = (byte)0x00;
                }
            } else {
                des[i] = src[i-desLen+srcLen];
            }
        }


//
//        BigInteger desb = new BigInteger(des);
//        if (srcb.compareTo(desb)!=0){
//            System.out.println("value not working "+srcb.toString()+","+desb.toString());
//        } else {
//            System.out.println("value working "+srcb.toString()+","+desb.toString());
//        }
//        if (des.length!=desLen){
//            System.out.println("len not working");
//        }

        return des;
//        return src;
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
	
	public static Object getObjectFromFile(String str) {
		Object o = null;
		try {
			FileInputStream fileIn = new FileInputStream(str);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			o = in.readObject();
			in.close();
			fileIn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return o;
	}

	public static byte[][] randomIntSet(int n) {
		HashSet<Integer> set = new HashSet<Integer>();
		byte[][] out = new byte[n][];
		Random rnd = new SecureRandom();
		// initialise set
		for (int i = 0; i < n; i++) {
			Integer e;
			do {
				e = rnd.nextInt();
			} while (set.contains(e));
			set.add(e);
			out[i]= Util.intToByteArray(e);
		}
		return out;
	}

    public static byte[] ecPointToByteArray(ECPoint ecPoint){
        byte[] bax = ecPoint.getX().toBigInteger().toByteArray();
        byte[] bay = ecPoint.getY().toBigInteger().toByteArray();

        if(bax.length>bay.length){
            Util.xor(bax, bay);
            return bax;
        }else{
            Util.xor(bay, bax);
            return bay;
        }
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
