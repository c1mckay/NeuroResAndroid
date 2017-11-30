package edu.ucsd.neurores.network;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d("token", "trying to refresh");
        Log.d("token", refreshedToken);

        final Intent intent = new Intent("tokenReceiver");
        // You can also include some extra data.
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        intent.putExtra("token", refreshedToken);
        broadcastManager.sendBroadcast(intent);

    }



}