package net.yishanhe.mobilesc.ot;

import android.util.Log;

import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.BitSet;

/**
 * Created by syi on 11/9/14.
 */
public class BaseOTR {
    private ECCurve curve;
    private ECPoint G;
    private BigInteger r;
    private SecureRandom rnd;
    private byte[] str;
    private BigInteger[] ks;
    private MessageDigest md;
    private final byte[] zero= new byte[1];
    private final byte[] one = new byte[]{1};
    private int leadingZeroes;
    private int strLen;


    public static BaseOTR easyCreate(byte[] sel){
        // We ned this apparently
        ECParameterSpec ecSpec = org.spongycastle.jce.ECNamedCurveTable.getParameterSpec("c2pnb163v1");

        int tmpLeadingZeros = 0;
        if(sel.length % 8 != 0){
            tmpLeadingZeros = (int)(((sel.length / 8) + 1) - sel.length);
        }


        MessageDigest md2 = null;
        try {
            md2 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // I'm not sure how to do this?  The byte array has preceding zeros?
        // Or it assums 1-bit per entry??
        return new BaseOTR(ecSpec, sel, tmpLeadingZeros, md2);

    }

    public BaseOTR(ECParameterSpec spec, byte[] str, int leadingZeroes, MessageDigest md) {
        this.curve = spec.getCurve();
        this.G = spec.getG();
        this.r = this.curve.getOrder();
        this.rnd = new SecureRandom();
        this.md = md;
        this.str = str;
        this.leadingZeroes = leadingZeroes;
        this.strLen = str.length*8 - this.leadingZeroes;
//        System.out.println("OT strlen is "+strLen);
        this.ks = new BigInteger[strLen];
    }

    public ECPoint[] preparePK0(ECPoint[] cs){
        ECPoint[] PK = new ECPoint[this.strLen];

        for (int i = 0; i < this.strLen; i++) {

            // get a random k
            do {
                ks[i] = new BigInteger(this.r.bitLength(), rnd);
            } while (ks[i].subtract(r).signum() >= 0);

            // get selection bit
            if(Util.getBit(i, this.leadingZeroes, this.str)) {
                // 1
                // c/g^k
                PK[i] = this.G.multiply(ks[i]);
                PK[i] = PK[i].negate();
                PK[i] = cs[i].add(PK[i]);

            } else {
                // 0
                // g^k
                PK[i] = this.G.multiply(ks[i]);
            }


        }
        
        return PK;
    }

    public byte[][] onReceiveEncByte(byte[][][] received, ECPoint[] grs){
        byte[][] result = new byte[this.strLen][];

        for (int i = 0; i < this.strLen; i++) {
            // pkr^k
            ECPoint pkr = grs[i].multiply(ks[i]);
            byte[] ba = Util.ecPointToByteArray(pkr);
            this.md.update(ba);
            if(Util.getBit(i,this.leadingZeroes,this.str)){
                // 1
                //result[i]=H.digest(this.one);
                result[i]= new byte[received[i][1].length];
                System.arraycopy(this.md.digest(this.one), 0, result[i], 0, result[i].length);
                Util.xor(result[i],received[i][1]);
            }else{
                //0
                //result[i]=H.digest(this.zero);
                result[i]= new byte[received[i][0].length];
                System.arraycopy(this.md.digest(this.zero), 0, result[i], 0, result[i].length);
                Util.xor(result[i],received[i][0]);

            }

        }
        return result;
    }
}
