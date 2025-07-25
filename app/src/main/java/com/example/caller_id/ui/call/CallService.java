package com.example.caller_id.ui.call;


import android.content.Intent;
import android.telecom.Call;
import android.telecom.InCallService;

public class CallService extends InCallService {

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        CallManager.setInCallServiceInstance(this);
        call.registerCallback(callCallback);
            Intent intent = new Intent(getApplicationContext(), CallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        CallManager.updateCall(this,call);
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        call.unregisterCallback(callCallback);
        CallManager.updateCall(this,null);
    }


    private Call.Callback callCallback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int state) {
            CallManager.updateCall(CallService.this,call);
        }
    };
}
