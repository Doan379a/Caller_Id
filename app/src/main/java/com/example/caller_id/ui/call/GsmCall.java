package com.example.caller_id.ui.call;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class GsmCall {

    @NotNull
    private final Status status;
    @org.jetbrains.annotations.Nullable
    private final String displayName;
    private final String displayNumber;

    @NotNull
    public final Status getStatus() {
        return this.status;
    }

    @org.jetbrains.annotations.Nullable
    public final String getDisplayName() {
        return this.displayName;
    }
    @org.jetbrains.annotations.Nullable
    public final String getDisplayNumber() {
        return this.displayNumber;
    }

    public GsmCall(@NotNull Status status, @Nullable String displayName, String displayNumber) {
        super();
        this.status = status;
        this.displayName = displayName;
        this.displayNumber = displayNumber;
    }

    public enum  Status {
        CONNECTING,
        DIALING,
        RINGING,
        ACTIVE,
        DISCONNECTED,
        UNKNOWN
    }
}
