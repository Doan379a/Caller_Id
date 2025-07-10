package com.example.caller_id.utils.helper;


import android.Manifest;
import android.os.Build;

public class Default {
    //about app
    public static final String EMAIL = "tranduyhung171199@gmail.com";
    public static final String SUBJECT = "Feedback: Funny Voice Changer";
    public static final String PRIVACY_POLICY = "https://sites.google.com/view/funny-voice-changer-onet/home";

    public static final String[] STORAGE_PERMISSION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}
            : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

}
