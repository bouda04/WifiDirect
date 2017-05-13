package com.example.bouda04.wifidirect.model;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by bouda04 on 11/3/2017.
 */

public class Member implements Parcelable{
    private WifiP2pDevice device;
    private String displayName;
    private int role;
    private InetAddress ipAddress;
    public final static int PUBLISHER_ROLE =1;
    public final static int SUBSCRIBER_ROLE =2;

    public Member(){};

    protected Member(Parcel in) {
        device = in.readParcelable(WifiP2pDevice.class.getClassLoader());
        displayName = in.readString();
        role = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(device, i);
        parcel.writeString(displayName);
        parcel.writeInt(role);
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        Member other = (Member)obj;
        return this.displayName.equals(other.displayName);
    }

    @Override
    public int hashCode() {
        return this.displayName.hashCode();
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    @Override
    public int describeContents() {
        return 0;
    }




    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

}
