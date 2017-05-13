package com.example.bouda04.wifidirect.framework;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.bouda04.wifidirect.model.Member;
import com.example.bouda04.wifidirect.model.WifiP2PProvider;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class WifiP2PHandler extends Service implements WifiP2pManager.ConnectionInfoListener{
    Context context;

    private static WifiP2pManager mManager;
    private static WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    WifiP2pManager.DnsSdServiceResponseListener srl;
    WifiP2pManager.DnsSdTxtRecordListener trl;
    Thread collector;
    WifiManager wifiManager;
    String ownerName;
    int myRole;
    boolean readyToDiscover;
    Object lock = new Object();
    boolean wifiP2PEnabled;
    boolean firstTime = true;
    WifiP2pDevice myDevice;
    boolean isConnectedNow;
    private final String TAG = "WifiP2PHandler";
    public static final String WIFI_NEW_MEMBER = "com.wifidirect.new-member";
    public static final String WIFI_IAM_GO = "com.wifidirect.iam-go";
    public static final String WIFI_IAM_CLIENT = "com.wifidirect.iam-client";

    private final String WIFI_TALKER_SERVICE = "talker";
    private final String WIFI_LISTNER_SERVICE = "listener";
    private final String WIFI_SERVICE_TYPE="_presence._tcp";

    public WifiP2PHandler() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Service's onStartCommand triggered...");

        ownerName = intent.getStringExtra("name");
        myRole = intent.getIntExtra("role", Member.SUBSCRIBER_ROLE);
        this.context = MyApplication.getAppContext();
        this.firstTime=true;
        initAndWait4WifiP2P();
        collector = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true){
                    try {
                        synchronized(lock) {
                            while(!readyToDiscover)
                                lock.wait();
                        }
                        Thread.sleep(5000);//10 seconds delay


                   //     if (myRole == Member.PUBLISHER_ROLE || firstTime){
                            register4WifiPeerEvents();
                            discoverMembers2();
                   //     }

                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                        Log.d(TAG,"Interrupting discovery thread...");
                        break;
                    }
                }
            }
        });

        collector.start();
        return Service.START_NOT_STICKY;
    }

    private void initAndWait4WifiP2P(){
        myDevice = null;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
       wifiManager.setWifiEnabled(false);
        isConnectedNow = false;
        wifiP2PEnabled=false;
        readyToDiscover=false;
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mReceiver = new WifiP2PHandler.WiFiDirectBroadcastReceiver();
        Log.d(TAG,"Registering Broadcast Receiver for ... WIFI_P2P_STATE_CHANGED_ACTION");
        context.registerReceiver(mReceiver, mIntentFilter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                wifiManager.setWifiEnabled(true);
                while (!wifiManager.isWifiEnabled() || !wifiP2PEnabled){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
                Log.d(TAG,"Channel initialize, Trying... ");

                mChannel = mManager.initialize(context, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
                    @Override
                    public void onChannelDisconnected() {
                        Log.d(TAG,"Wifi-Direct Channel disconnected !!! ");
                    }
                });
                initDiscoveredServicesListeners();
                Log.d(TAG,"init discovery... ");
                initDiscovery(ownerName, myRole);
            }
        }).start();
    }
    private void register4WifiPeerEvents(){

        try{
            context.unregisterReceiver(mReceiver);
        }catch(IllegalArgumentException e){}//do nothing

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


   //     if (mReceiver == null)
            mReceiver = new WifiP2PHandler.WiFiDirectBroadcastReceiver();
        Log.d(TAG,"register4WifiPeerEvents...");

        context.registerReceiver(mReceiver, mIntentFilter);
    }

    private void initDiscoveredServicesListeners(){
        srl = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String s, String s1, WifiP2pDevice wifiP2pDevice) {
                //  if (toConnect) connect(wifiP2pDevice, toConnect);
                Log.d(TAG, "onDnsSdServiceAvailable: ...device " + wifiP2pDevice.deviceName + "/" + wifiP2pDevice.deviceAddress);
            }
        };
        trl = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String s, Map<String, String> map, WifiP2pDevice wifiP2pDevice) {
                Log.d(TAG, "onDnsSdTxtRecordAvailable: ...got txt record from: "+ s + ", name = " +map.get("name") );

                if (myRole == Member.PUBLISHER_ROLE && s.contains(WIFI_LISTNER_SERVICE) ||
                    myRole == Member.SUBSCRIBER_ROLE && s.contains(WIFI_TALKER_SERVICE)){
                    Member member = new Member();
                    member.setDevice(wifiP2pDevice);
                    member.setDisplayName(map.get("name"));
                    Intent i = new Intent(WIFI_NEW_MEMBER);

                    i.putExtra("member", member);
                    sendBroadcast(i);
                }

            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new WifiBinder();
    }

    //the following method is called when calling for 'requestConnectionInfo'
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
// InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
//        Log.d(TAG, "onConnectionInfoAvailable: ...Received WifiP2pInfo from "+ groupOwnerAddress);
        Log.d(TAG, "onConnectionInfoAvailable: ...\n "+ wifiP2pInfo);
        // After the group negotiation, we can determine the group owner
        // (server).
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            Intent i = new Intent(WIFI_IAM_GO);

            i.putExtra("GOaddress", groupOwnerAddress);
            sendBroadcast(i);
            Log.d(TAG, "onConnectionInfoAvailable: ... I am the GroupOwner");
        } else if (wifiP2pInfo.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            Intent i = new Intent(WIFI_IAM_CLIENT);

            i.putExtra("GOaddress", groupOwnerAddress);
            sendBroadcast(i);
        }
    }

    public class WifiBinder extends Binder {
        public WifiP2PHandler getService() {
            return WifiP2PHandler.this;
        }
    }


    public void initDiscovery(final String ownerName, final int myRole){
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                HashMap<String, String> record = new HashMap<>();
                record.put("name", ownerName);
                WifiP2pDnsSdServiceInfo serviceInfo;
                if (myRole == Member.PUBLISHER_ROLE )
                    serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(WIFI_TALKER_SERVICE, WIFI_SERVICE_TYPE, record);
                else
                    serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(WIFI_LISTNER_SERVICE, WIFI_SERVICE_TYPE, record);

                //remove legacy group
                mManager.removeGroup(mChannel, null);
                mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        if (myRole == Member.PUBLISHER_ROLE)
                            //create group, making this device the owner of the group
                            mManager.createGroup(mChannel, null);
                        mManager.setDnsSdResponseListeners(mChannel, srl, trl);
                        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                mManager.addServiceRequest(mChannel, WifiP2pDnsSdServiceRequest.newInstance(), new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG,"Add Service request, succeeded");
                                        synchronized(lock) {
                                            readyToDiscover = true;
                                            lock.notifyAll();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int code) {
                                        Log.d(TAG,"Add Service request, Failed with error: " + code);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int code) {
                                Log.d(TAG,"Clear Service Requests, Failed with error: " + code);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.d(TAG,"Add Local service, Failed with error: " + code);
                    }
                });
            }

            @Override
            public void onFailure(int code) {
                Log.d(TAG,"Clear Local Services, Failed with error: " + code);
            }
        });
    }


    public void discoverMembers(){
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int i) {
                                Log.d(TAG,"discoverServices, Failed with error: " + i);
                            }
                        });
                    }
                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG,"discoverPeers, Failed with error: " + i);
                    }
                });
            }

            @Override
            public void onFailure(int i) {
                Log.d(TAG,"discoverPeers, Failed with error: " + i);
            }
        });
    }


    public void discoverMembers2(){
        Log.d(TAG, "discovering new members... ");
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        firstTime = false;
                        Log.d(TAG,"discoverServices, succeeded");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d(TAG,"discoverServices, Failed with error: " + i);
                    }
                });
            }
            @Override
            public void onFailure(int i) {
                Log.d(TAG,"discoverPeers, Failed with error: " + i);
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"got onDestroy of the Handler service...");
        collector.interrupt();
        if (mManager != null && mChannel != null){
            mManager.stopPeerDiscovery(mChannel,null);

            //       mManager.clearLocalServices(mChannel,null);
            //       mManager.clearServiceRequests(mChannel, null);
            mManager.removeGroup(mChannel,null);
        }

        context.unregisterReceiver(mReceiver);
        super.onDestroy();
    }


    public void connect(final WifiP2pDevice device, int role) {

        WifiP2pConfig config = new WifiP2pConfig();

        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = role==Member.PUBLISHER_ROLE?15:1;
        config.deviceAddress = device.deviceAddress;
        Log.d(TAG, "Connecting to " + device.deviceAddress);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    Log.d(TAG, "Connect OK to " + device);
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Connect to " + device +" \nfailed, reason:" + reason);
                }
            });
    }

    public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                boolean enabled = (intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                        WifiP2pManager.WIFI_P2P_STATE_DISABLED)) ==
                        WifiP2pManager.WIFI_P2P_STATE_ENABLED;
                Log.d(TAG, "Received WIFI_P2P_STATE_CHANGED_ACTION: enabled="
                        + enabled);
                WifiP2PHandler.this.wifiP2PEnabled = enabled;
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                        Log.d(TAG,String.format("PeerListListener: %d peers available, updating device list", wifiP2pDeviceList.getDeviceList().size()));

                        // DO WHATEVER YOU WANT HERE
                        // YOU CAN GET ACCESS TO ALL THE DEVICES YOU FOUND FROM peers OBJECT
                    }
                });
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: isConnected="
                        + networkInfo.isConnected());
                if (networkInfo.isConnected()) {
                    if (!isConnectedNow) {
                        isConnectedNow = true;
                        // we are connected with the other device, request connection
                        // info to find group owner IP
                        Log.d(TAG, "Connected to p2p network. Requesting connection info");
                        mManager.requestConnectionInfo(mChannel, WifiP2PHandler.this);
                    }
                } else if (isConnectedNow){
                    isConnectedNow = false;
                    Log.d(TAG,"disconnected from p2p network.");
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                myDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.d(TAG, "Received WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:" +myDevice);
            }
        }
    }
}
