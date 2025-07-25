package com.example.caller_id.ui.call;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;


public class CallManager {

    private static BehaviorSubject subject;
    private static Call currentCall = null;
    public static CallManager INSTANCE;
    private static InCallService inCallService;

    public static void setInCallServiceInstance(InCallService service) {
        inCallService = service;
    }
    public static Observable updates() {
        BehaviorSubject behaviorSubject = subject;
        return (Observable)behaviorSubject;
    }

    public static void updateCall( @Nullable Context context,@Nullable Call call) {
        currentCall = call;
        if (call != null) {
            try {
                subject.onNext(MappersJava.toGsmCall(context,call));
            } catch (Exception e) {
                subject.onError(e);
            }
        }
    }

    public static void cancelCall() {
        Call call = currentCall;
        if (call != null) {
            if (call.getState() == Call.STATE_RINGING) {
                INSTANCE.rejectCall();
            } else {
                INSTANCE.disconnectCall();
            }
        }

    }
    public static void playDtm(String number) {
        Call call = currentCall;
        if (call != null && number != null && !number.isEmpty()) {
            char dtmfChar = number.charAt(0);
            call.playDtmfTone(dtmfChar);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                call.stopDtmfTone();
            }, 300);
        }
    }

    public static void acceptCall() {
        Call call = currentCall;
        if (call != null) {
            call.answer(call.getDetails().getVideoState());
        }

    }

    private static void rejectCall() {
        Call call = currentCall;
        if (call != null) {
            call.reject(false, "");
        }

    }

    private static void disconnectCall() {
        Call call = currentCall;
        if (call != null) {
            call.disconnect();
        }

    }
    public static void setSpeaker( boolean isSpeakerOn) {
        if (inCallService != null) {
            int route = isSpeakerOn ? CallAudioState.ROUTE_SPEAKER : CallAudioState.ROUTE_EARPIECE;
            inCallService.setAudioRoute(route);
            Log.d("AudioDebug", "Forced route: " + (isSpeakerOn ? "SPEAKER" : "EARPIECE"));
        } else {
            Log.e("AudioDebug", "inCallService is null; cannot set audio route.");
        }
    }

    public static void setMicrophoneMute(boolean isMuted) {
        if (inCallService != null) {
            inCallService.setMuted(isMuted);
            Log.d("AudioDebug", "Mic muted via InCallService: " + isMuted);
        } else {
            Log.e("AudioDebug", "inCallService is null; cannot mute mic.");
        }
    }


    static {
        CallManager var0 = new CallManager();
        INSTANCE = var0;
        subject = BehaviorSubject.create();
    }
}
