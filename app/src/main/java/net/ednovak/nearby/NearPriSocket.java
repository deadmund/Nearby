package net.ednovak.nearby;

import android.net.NetworkRequest;
import android.util.Log;
import android.widget.Toast;

import org.spongycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by ejnovak on 10/6/15.
 */
public class NearPriSocket extends Socket {
    private final static String TAG = NearPriSocket.class.getName();

    private final static int BYTE_COUNT = 8192;

    public final static String SERVER_HOST =  "ec2-52-89-134-220.us-west-2.compute.amazonaws.com";
    public final static int SERVER_PORT = 5555;


    private OutputStream sockOut;
    private InputStream sockIn;


    NearPriSocket(InetAddress addr, int port) throws IOException{
        super(addr, port);

        sockOut = this.getOutputStream();
        sockIn = this.getInputStream();
    }


    public static InetAddress getAddressFromHost(String host){
        try{
            return InetAddress.getByName(host);
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
        return null;
    }


    public byte[] readSocket(){
        byte[] buffer = new byte[BYTE_COUNT];
        int sumBytes = 0;
        while(sumBytes < BYTE_COUNT){
            int newAmount = 0;
            try{
                newAmount = sockIn.read(buffer, sumBytes, BYTE_COUNT - sumBytes);
                sumBytes += newAmount;
                if(newAmount < 0){
                    return buffer;
                }
                //Log.d(TAG, "Read " + newAmount + " just now.  Total of " + sumBytes + " so far.");
                //Log.d(TAG, "Last Buffer Boundry Section : " + NearPriLib.section(buffer, sumBytes - (newAmount + 3), sumBytes - (newAmount - 3)));
                //Log.d(TAG, "current buffer boundry Section: " + NearPriLib.section(buffer, sumBytes - 3, sumBytes + 3));

            } catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }
        return buffer;
    }

    public void writeSocket(byte[] data){
        if(data.length < BYTE_COUNT){
            byte[] newData = new byte[BYTE_COUNT];
            for(int i = 0; i < data.length; i++){
                newData[i] = data[i];
            }
            data = newData;
        }
        try {
            sockOut.write(data);
            sockOut.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /*
    public void writeStringToSocket(String msg){
        byte[] msgBytes = msg.getBytes();
        writeWithPadding(msgBytes);
        Log.d(TAG, "Message Sent: " + msg);
    }
    */

    /*
    public void writeECPointToSocket(ECPoint ecp) {
        // Pad the stupid thing to BYTE_COUNT with null bytes
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);

            oos.writeObject(ecp);
            oos.flush();
            oos.close();

            byte[] ecpByteData = byteArrayOutputStream.toByteArray();
            writeWithPadding(ecpByteData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */


    private void writeWithPadding(byte[] input){
        byte[] data = new byte[BYTE_COUNT];

        for(int i = 0; i < input.length; i++){
            data[i] = input[i]; // The rest should be empty!
        }

        try {
            sockOut.write(data);
        } catch (IOException e){
            e.printStackTrace();
            closeSocket();
        } catch(NullPointerException e1){
            e1.printStackTrace();
            closeSocket();
        }
    }



    /*
    public ECPoint readECPointFromSocket(){
        ECPoint tmp = null;
        try{
            ObjectInputStream ois = new ObjectInputStream(sockIn);
            tmp = (ECPoint)ois.readObject();
        } catch (Exception e){
            e.printStackTrace();
        }
        return tmp;
    }
    */


    public void closeSocket(){
        if(this != null) {
            while (!this.isClosed()) {
                try {
                    this.close();
                } catch (IOException e) {
                }
            }
        }
        sockOut = null;
        sockIn = null;
        Log.d(TAG, "Socket closed");
    }
}
