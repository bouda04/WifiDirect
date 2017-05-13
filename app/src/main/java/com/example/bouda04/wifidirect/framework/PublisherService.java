package com.example.bouda04.wifidirect.framework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.bouda04.wifidirect.views.InfoActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PublisherService extends InfoService {
    private static final int PORT_NUMBER=8888;
    private InetAddress serverIP;
    private final String TAG = "PublisherService";
    Thread broadCaster = null;
    Thread socketCollector = null;

    private ArrayList<Socket> receiversSockets = new ArrayList<Socket>();
    public PublisherService() {}

    @Override
    public void onDestroy() {
        broadCaster.interrupt();
        socketCollector.interrupt();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serverIP = (InetAddress) intent.getSerializableExtra("serverIP");

        socketCollector = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(PORT_NUMBER, 50, serverIP);
                    Log.d(TAG, "opening server socket for ip: " + serverIP.getHostAddress());
                    while (true){
                        Socket client = serverSocket.accept();
                        receiversSockets.add(client);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        socketCollector.start();
        broadCaster = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int data=0;
                    while (true) {
                        synchronized(lock) {
                            while(!goAhead)
                                lock.wait();
                        }
                        for (Socket socket : receiversSockets) {
                            OutputStream outputStream = socket.getOutputStream();
                            outputStream.write(data);
                        }
                        Intent i = new Intent(InfoActivity.NEW_INFO);

                        i.putExtra("info", data);
                        sendBroadcast(i);
                        Thread.sleep(1000);
                        data++;
                }
                }catch(IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        broadCaster.start();

        return Service.START_NOT_STICKY;
    }
}
