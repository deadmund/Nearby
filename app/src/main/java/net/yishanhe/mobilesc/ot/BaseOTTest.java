package net.yishanhe.mobilesc.ot;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.ECPointUtil;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.ECFieldElement;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * Created by syi on 11/9/14.
 */
public class BaseOTTest {
    public static void main(String[] args) {



        BigInteger a = new BigInteger("11111111",2);
        System.out.println(a);
        System.out.println(a.toString(2));

        byte[] ca = a.toByteArray();
        System.out.println(Util.bytesToHex(ca));
        System.out.println(new BigInteger(ca));


        BigInteger b = new BigInteger("11111111",2).negate();
        System.out.println(b);
        System.out.println(b.toString(2));

        byte[] cb = b.toByteArray();
        System.out.println(Util.bytesToHex(cb));
        System.out.println(new BigInteger(cb));

        byte[] cbb = Util.expandByteArray(cb, 32);
        System.out.println(Util.bytesToHex(cbb));
        System.out.println(new BigInteger(cbb));





        testECC();

    }



    public static void testECC(){

//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime192v1");
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("c2pnb163v1");



        // k pairs of string to send
        // k is the rows of the matrix
        // vector length of selection vec
        int k = 16;
        // each string is 20 bytes
        int l = 32;
        int sigma = l/8;

        SecureRandom rnd = new SecureRandom();

        byte[][][] toSend = new byte[k][2][sigma];


        // toSend is the input
        // generate random input
        for (int i = 0; i < k; i++) {
            rnd.nextBytes(toSend[i][0]); // true data
            rnd.nextBytes(toSend[i][1]); // dummies
//            System.out.println("Generation: "+Util.compareByteArrays(toSend[i][0],toSend[i][1]));
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        BaseOTS sender = new BaseOTS(ecSpec, md, k, toSend);

        // the selection vector
        // generate selection for receiver
        byte[] selection;
        int leadingZeroes = 0;
        if(k%8 ==0){
            selection = new byte[k/8];
        } else {
            selection = new byte[k/8+1];
            leadingZeroes = selection.length*8-k;
        }

        rnd.nextBytes(selection);

        MessageDigest md2 = null;
        try {
            md2 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BaseOTR receiver = new BaseOTR(ecSpec, selection, leadingZeroes, md2);

        long start = System.nanoTime();

        // sender prepare the some random numbers
        ECPoint[] cs = sender.getCs();

        // receiver prepares the public keys
        ECPoint[] pk0s = receiver.preparePK0(cs);

        // sender allocate space for cipher texts
        byte[][][] encrypted = new byte[k][2][sigma];

        // sender gets pulic keys from receiver
        ECPoint[] grs = sender.onReceivePK0s(pk0s, encrypted);

        // send grs and encrypted to receiver
        byte[][] result = receiver.onReceiveEncByte(encrypted, grs);

        long end = System.nanoTime();

        System.out.println("OT time elapsed: " + ((end - start) / 1000000000.0) + " sec");
        
        // check correctness
        // bit is 1 then the result is toSend[i][1]
        for (int i = 0; i < k; i++) {
            if(Util.getBit(i,leadingZeroes,selection)){
                //1

                if(!Util.compareByteArrays(result[i],toSend[i][1]))

                    System.out.println(i+" Bit 1 incorrect");

                    System.out.println(new BigInteger(result[i])+", "+new BigInteger(toSend[i][1]));

            }else{
                //0
                if(!Util.compareByteArrays(result[i],toSend[i][0])){
                    System.out.println(i+" Bit 0 incorrect");

                    System.out.println(new BigInteger(result[i])+", "+new BigInteger(toSend[i][0]));
                }
            }
        }


    }

}
