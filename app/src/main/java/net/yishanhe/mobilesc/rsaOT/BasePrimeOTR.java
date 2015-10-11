package net.yishanhe.mobilesc.rsaOT;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Created by syi on 11/20/14.
 */
public class BasePrimeOTR {

    private BigInteger p;
    private BigInteger q;
    private BigInteger g;
    private int[] select;
    private int selectLen;
    private SecureRandom rnd;
    private MessageDigest md;
    private BigInteger[] ks; // random k
    private int N;



    public BasePrimeOTR(BigInteger p, BigInteger q, BigInteger g, int[] select, MessageDigest md, int N) {
        this.p = p;
        this.q = q;
        this.g = g;
        this.select = select;
        this.md = md;
        this.rnd = new SecureRandom();
        this.selectLen = select.length;
        this.ks = new BigInteger[this.selectLen];
        this.N = N;
    }

    public BigInteger[] preparePK0(BigInteger[] cs){
        
        BigInteger[] PK0s = new BigInteger[this.selectLen];
        BigInteger DK;

        for (int i = 0; i < this.selectLen; i++) {


            // get a random k
            do{
                this.ks[i] = new BigInteger(this.q.bitLength(),rnd);
            } while (this.ks[i].subtract(q).signum() >= 0);

            DK = this.g.modPow(this.ks[i], this.p);

            if ( this.select[i] == 0 ) {
                // pk0 = dk
                PK0s[i] = DK;
            } else {
                // ci/dk
                PK0s[i] = cs[select[i]].multiply(DK.modInverse(this.p)).mod(this.p);
            }

        }



        return PK0s;
    }

    public byte[][] onReceiveEncByte(byte[][][] received, BigInteger[] cs){


        byte[][] result = new byte[this.selectLen][];

        for (int i = 0; i < this.selectLen; i++) {

            // g^rk
            BigInteger pkr = cs[0].modPow(this.ks[i], this.p);

            byte[] ba = pkr.toByteArray();

            this.md.update(ba);

            result[i] = new byte[received[i][this.select[i]].length];
            System.arraycopy(this.md.digest(BigInteger.valueOf(select[i]).toByteArray()),0,result[i],0,result[i].length);

            Util.xor(result[i], received[i][this.select[i]]);


        }

        return result;
    }

    public byte[][][] tryDecAll(byte[][][] received, BigInteger[] cs){
        byte[][][] result = new byte[this.selectLen][this.N][];

        for (int i = 0; i < this.selectLen; i++) {

            for (int j = 0; j < this.N; j++) {

            BigInteger pkr = cs[0].modPow(this.ks[i], this.p);

            byte[] ba = pkr.toByteArray();

            this.md.update(ba);


                result[i][j] = new byte[received[i][j].length];
                System.arraycopy(this.md.digest(BigInteger.valueOf(j).toByteArray()),0,result[i][j],0,result[i][j].length);
                Util.xor(result[i][j], received[i][j]);
            }
        }

        return result;
    }
}
