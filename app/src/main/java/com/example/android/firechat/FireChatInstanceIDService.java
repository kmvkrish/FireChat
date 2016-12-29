package com.example.android.firechat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by kmvkrish on 27-12-2016.
 */

public class FireChatInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = FireChatInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){
        //TODO:
    }
}
