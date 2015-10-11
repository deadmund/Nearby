package net.yishanhe.mobilesc.ot;

import android.util.Log;

import net.ednovak.nearby.NearPriLib;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;


/**
 * Created by syi on 11/8/14.
 * Naor and Pinkas
 * Since we will use ExtOT this is a 1-out-2 OT
 */
public class BaseOTS {
    private final static String TAG = BaseOTS.class.getName();

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private ECCurve curve;
    private ECPoint G; // generator
    private BigInteger r; // curve order

    private SecureRandom rnd;
    private MessageDigest md;

    private final byte[] zero= new byte[1];
    private final byte[] one = new byte[]{1};
    private byte[][][] toSend;

    // need to create baseOTSmsg


    // number of OTs needed
    private int k;

    // For OT
    private BigInteger[] rs;
    private ECPoint[] cs;
    private ECPoint[] crs;  // in Fp, crs= c^r (pre-computing)
    private ECPoint[] grs;  // in Fp, grs = g^r (pre-computing)

    public static BaseOTS easyCreate(byte[][][] toSend){
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("c2pnb163v1");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        int tmpK = toSend.length;
        return new BaseOTS(ecSpec, md, tmpK, toSend);
    }

    public BaseOTS(ECParameterSpec spec, MessageDigest md, int k, byte[][][] toSend) {
        long start = System.currentTimeMillis();

        this.curve = spec.getCurve();
        this.md = md;
        this.k = k;
        this.r = this.curve.getOrder();
        this.G = spec.getG();
        this.rnd = new SecureRandom();

        this.rs = new BigInteger[k];
        this.cs = new ECPoint[k];
        this.crs = new ECPoint[k];
        this.grs = new ECPoint[k];

        this.toSend=toSend; //msg


        for (int i = 0; i < this.k; i++) {
            do {
                this.rs[i] = new BigInteger(this.r.bitLength(),rnd);
            } while(this.rs[i].subtract(this.r).signum()>=0);

            // get random C using random rs
            this.cs[i] = this.G.multiply(this.rs[i]);

            // update rs as r
            do {
                this.rs[i] = new BigInteger(this.r.bitLength(),rnd);
            } while(this.rs[i].subtract(this.r).signum()>=0);

            this.crs[i] = this.cs[i].multiply(this.rs[i]); // c^r
            this.grs[i] = this.G.multiply(this.rs[i]); // g^r

        }

        long runTime = NearPriLib.getTimeSince(start);
        Log.d(TAG, "Finished creating BaseOTS in " + runTime + "ms");
    }

    public ECPoint[] onReceivePK0s(ECPoint[] PK0s, byte[][][] encrypted){
        for (int i = 0; i < k; i++) {
            BigInteger R = this.rs[i];
            ECPoint pk0r = PK0s[i].multiply(R);  //pk0^r)
            ECPoint pk1r = pk0r.negate();
            pk1r = this.crs[i].add(pk1r); // pk1^r

            // H(pk0r,0)
            byte[] ba = Util.ecPointToByteArray(pk0r);
            this.md.update(ba);
            encrypted[i][0]= new byte[toSend[i][0].length];
            System.arraycopy(this.md.digest(this.zero), 0, encrypted[i][0], 0, toSend[i][0].length);
            Util.xor(encrypted[i][0],this.toSend[i][0]);

            ba = Util.ecPointToByteArray(pk1r);
            this.md.update(ba);
            encrypted[i][1]= new byte[toSend[i][1].length];
            System.arraycopy(this.md.digest(this.one), 0, encrypted[i][1], 0, toSend[i][1].length);
            Util.xor(encrypted[i][1],this.toSend[i][1]);

        }

        // return g^r for sending
        return this.grs;

    }

    public ECPoint[] getCs() {
        return cs;
    }
}
