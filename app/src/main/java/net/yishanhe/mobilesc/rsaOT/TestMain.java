package net.yishanhe.mobilesc.rsaOT;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.DSAPublicKey;

/**
 * 1-out-of-N OT
 * credit to PSI implementation
 * https://personal.cis.strath.ac.uk/changyu.dong/PSI/PSI.html
 *
 */
public class TestMain {

    public static void main(String[] args) {
        System.out.println("Test OT");


        int k = 10; // run OT k times
        int N = 2;  // 1-out-of-N OT

        KeyPairGenerator keyPairGenerator = null;

        try{
            keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        SecureRandom rnd = new SecureRandom();
        if (keyPairGenerator != null) {
            keyPairGenerator.initialize(1024,rnd);
        } else {
            System.out.println("KeyGen failed.");
            return;
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        DSAPublicKey pub = (DSAPublicKey)keyPair.getPublic();

        BigInteger p = pub.getParams().getP();
        BigInteger q = pub.getParams().getG();
        BigInteger g = pub.getParams().getG();

        int l = 160;
        int sigma = l/8;


        // initiate the toSend using random bytes
        byte[][][] toSend = new byte[k][N][sigma];

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < N; j++) {
                rnd.nextBytes(toSend[i][j]);
            }
        }


        // prepare md　
        MessageDigest md = null;

        // depend on k
        try {
            md = MessageDigest.getInstance("SHA1"); //SHA1 is just 160bits output
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        // init sender
        BasePrimeOTS sender = new BasePrimeOTS(p,q,g,md,k,N,toSend);

        // init a selection vector using random int
        System.out.println("\n\nChoices:");
        int[] selection = new int[k];
        for (int i = 0; i < k; i++) {
            selection[i] = rnd.nextInt(N);
            System.out.println("\t"+selection[i]);
        }

        BasePrimeOTR receiver = new BasePrimeOTR(p,q,g,selection,md,N);



        // start

        BigInteger[] cs = sender.getCs();

        BigInteger[] pk0s = receiver.preparePK0(cs);

        byte[][][] encrypted = new byte[k][N][sigma];

        sender.onReceivePK0s(pk0s,encrypted);

        byte[][] result = receiver.onReceiveEncByte(encrypted,cs);

        byte[][][] allresult = receiver.tryDecAll(encrypted,cs);

        System.out.println("\n\nReceived ");
        for (int i = 0; i < k; i++) {

            if(Util.compareByteArrays(result[i], toSend[i][selection[i]])){
                System.out.println("\tCorrect at round "+i+": choice "+selection[i]);
                System.out.println("\t"+Util.bytesToHex(result[i]) + " v.s " + Util.bytesToHex(toSend[i][selection[i]]));

            }
        }
//
//        // check all other are corrupted.
        System.out.println("\n\nTest if all others are corrupted.");

        for (int i = 0; i < k; i++) {
            System.out.println("Round " + i + ": ");
            for (int j = 0; j < N; j++) {
                if(!Util.compareByteArrays(allresult[i][j], toSend[i][j])){
                    System.out.println("\t Corrupted at choice "+j);
                    System.out.println("\t\t " + Util.bytesToHex(allresult[i][j]) + " v.s " + Util.bytesToHex(toSend[i][j]));
//                    System.out.println(bytesToHex(result[i]) + " v.s " + bytesToHex(toSend[i][selection[i]]));

                } else {
                    System.out.println("\t Correct at choice "+j);
                }
            }
        }








    }



}
