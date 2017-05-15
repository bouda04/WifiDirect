package com.example.bouda04.wifidirect.views;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.bouda04.wifidirect.R;
import com.example.bouda04.wifidirect.controllers.MembersAdapter;

import java.net.InetAddress;

public class MembersFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_OWNER_ROLE = "role";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters

    private String mParam2;

    private OnMembersInteractionListener mListener;
    private ListView membersList;

    public MembersFrag() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MembersFrag newInstance(int role) {
        MembersFrag fragment = new MembersFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_OWNER_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
          //  ownerRole = getArguments().getInt(ARG_OWNER_ROLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_members, container, false);
        this.membersList = (ListView) view.findViewById(R.id.listMembers);
        ((Button)view.findViewById(R.id.btnRestartDiscover)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MembersAdapter)membersList.getAdapter()).restartDiscovery();
            }
        });

        membersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MembersAdapter)membersList.getAdapter()).onMemberClicked(i);
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initListener(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initListener(activity);
    }

    private void initListener(Context context){
        if (context instanceof OnMembersInteractionListener) {
            mListener = (OnMembersInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMembersInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        membersList.setAdapter(new MembersAdapter(getActivity()));
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((MembersAdapter)membersList.getAdapter()).close();
        mListener = null;
    }

    public interface OnMembersInteractionListener {
        public int getOwnerRole();
        public String getOwnerName();
        public void onWifiEstablished(InetAddress ipAddress);
        public void onWifiRestarted();
        public void onConnectingToPublisher(String name);
    }
}
