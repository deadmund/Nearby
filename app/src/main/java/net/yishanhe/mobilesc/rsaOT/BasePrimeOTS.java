package net.yishanhe.mobilesc.rsaOT;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Created by syi on 11/19/14.
 * A Naor-Pinkas OT receiver in Prime number group.
 * 1-out-of-N OT
 * credit to PSI implementation
 * https://personal.cis.strath.ac.uk/changyu.dong/PSI/PSI.html
 *
 * Change log
 * 1. We reuse C in the protocol, by submit a random string to the hash function. every round.
 *
 */
public class BasePrimeOTS {

    private BigInteger p; // group
    private BigInteger q; // subgroup
    private BigInteger g; // generator
    
    private MessageDigest md; //  hash function
    private int k; // rounds of OT.
    private int N; // 1-out-of-n OT.
    private BigInteger r;
    private BigInteger[] cs; // N  [0, 1 -> N-1], first is g,
    private BigInteger[] crs; // N []
//    private BigInteger[] grs; // k
    private SecureRandom rnd;
    private byte[][][] toSend; // k*(N-1)*messageByteLen

    public BasePrimeOTS(BigInteger p, BigInteger q, BigInteger g, MessageDigest md, int k, int N, byte[][][] toSend) {

        this.p = p; // group
        this.q = q; // subgroup
        this.g = g;
        this.md = md;
        this.k = k;
        if(k != toSend.length){
            throw new IllegalArgumentException("k (" + k + ") must equal len(toSend) (" + toSend.length + ")");
        }
        this.N = N;
        this.toSend = toSend;
        this.rnd = new SecureRandom();


        this.cs = new BigInteger[N];
        this.crs = new BigInteger[N];


        // todo R for hash Naor-Pinkas Protocol 3.1


        // init cs, except cs[0]
        for (int i = 0; i < N; i++) {

            do {
                // use crs[0] as buffer to generate cs
                this.crs[0] = new BigInteger(this.q.bitLength(), rnd);
            } while (this.crs[0].subtract(this.q).signum()>=0);


            // generate r
            if( i == 0 ){
                this.r = this.crs[0];
            }

            //cs = [g^r, c1,c2,...,cN-1]
            this.cs[i] = this.g.modPow(this.crs[0], this.p);

            if( i == 0 ){
                this.crs[i] = this.cs[i];
            } else {
                this.crs[i] = this.cs[i].modPow(this.r, this.p);
            }

        }


                
    }

    /**
     * k is the number of OTs
     * each OT has a PK0
     * @param PK0s PKO array for OTs
     * @param encrypted encrypted output H(PK0^r,0) xor M0,  H(PK1^r,0) xor M0
     *                  encrypted[k][N-1][byteLen]
     * @return
     */
    public void onReceivePK0s(BigInteger[] PK0s, byte[][][] encrypted){

        for (int i = 0; i < k; i++) {

            // PK0->PK0^r
            BigInteger pk0r = PK0s[i].modPow(this.r, this.p);

            byte[] ba = pk0r.toByteArray();

            this.md.update(ba);

            encrypted[i][0] = new byte[this.toSend[i][0].length];

            // TODO add R for reusing C
            // be careful that the BigInteger.
            System.arraycopy(this.md.digest(BigInteger.valueOf(0).toByteArray()),0, encrypted[i][0],0,this.toSend[i][0].length);
            Util.xor(encrypted[i][0], this.toSend[i][0]);

            // get all others pk_i 1 ... N-1 ^r
            for (int j = 1; j < N; j++) {
                BigInteger pkjr = this.crs[j].multiply(pk0r.modInverse(this.p)).mod(this.p);
                ba = pkjr.toByteArray();
                this.md.update(ba);

                encrypted[i][j] = new byte[this.toSend[i][j].length];
                System.arraycopy(this.md.digest(BigInteger.valueOf(j).toByteArray()),0, encrypted[i][j],0, this.toSend[i][j].length);
                Util.xor(encrypted[i][j],this.toSend[i][j]);
            }
        }

    }


    public BigInteger[] getCs() {
        return cs;
    }
}
