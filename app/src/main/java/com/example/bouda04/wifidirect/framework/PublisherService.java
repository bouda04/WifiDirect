package com.example.bouda04.wifidirect.framework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.bouda04.wifidirect.views.InfoActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

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
                        Log.d(TAG, "got a new socket open request, from " + client);
                        receiversSockets.add(client);
                        Intent i = new Intent(InfoService.CLIENTS_COUNT);

                        i.putExtra("clients-count", receiversSockets.size());
                        sendBroadcast(i);
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
                        Iterator<Socket> iter = receiversSockets.iterator();
                        while (iter.hasNext()){
                            OutputStream outputStream=null;
                            Socket socket  = iter.next();
                            Log.d(TAG, "send data to socket: " + socket);
                            try {
                                outputStream = socket.getOutputStream();
                                DataOutputStream dout = new DataOutputStream(outputStream);
                                dout.writeInt(data);
                            }catch(IOException e){
                                Log.d(TAG, "removing this socket: " + socket);
                                if (outputStream != null)
                                    try {
                                        outputStream.close();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                iter.remove();
                                e.printStackTrace();
                                Intent i = new Intent(InfoService.CLIENTS_COUNT);

                                i.putExtra("clients-count", receiversSockets.size());
                                sendBroadcast(i);
                            }
                        }
                        Intent i = new Intent(InfoService.NEW_INFO);

                        i.putExtra("info", data);
                        sendBroadcast(i);
                        Thread.sleep(1000);
                        data++;
                }
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
