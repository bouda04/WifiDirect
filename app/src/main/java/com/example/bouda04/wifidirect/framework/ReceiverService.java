package com.example.bouda04.wifidirect.framework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.bouda04.wifidirect.views.InfoActivity;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiverService extends InfoService {
    private final String TAG = "ReceiverService";
    private static final int PORT_NUMBER=8888;
    private InetAddress serverIP;
    Thread dataReceiver = null;

     public ReceiverService() {}

    @Override
    public void onDestroy() {
        if (dataReceiver != null) {
            dataReceiver.interrupt();
            dataReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        serverIP = (InetAddress) intent.getSerializableExtra("serverIP");
        Log.d(TAG, "I am a client, connecting to GO address:" + serverIP.getHostAddress());

        dataReceiver = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = new Socket();
                try {
                    Log.d(TAG, "trying to open socket for ip: " + serverIP);
                    socket.connect(new InetSocketAddress(serverIP, PORT_NUMBER));
                    Log.d(TAG, "socket was successfully opened");
                    InputStream inputStream = socket.getInputStream();
                    DataInputStream din = new DataInputStream(inputStream);
                    while (true){
                        synchronized(lock) {
                            while(!goAhead)
                                lock.wait();
                        }
                        Log.d(TAG, "waiting for data from socket...");
                        int data = din.readInt();

                        Intent i = new Intent(InfoService.NEW_INFO);

                        i.putExtra("info", data);
                        sendBroadcast(i);
                        Log.d(TAG, "got this data: " + data);
                    }
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    return;
                }
            }
        });
        dataReceiver.start();
        return Service.START_NOT_STICKY;
    }
}
