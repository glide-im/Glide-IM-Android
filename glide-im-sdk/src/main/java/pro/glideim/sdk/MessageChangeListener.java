package pro.glideim.sdk;

import androidx.annotation.NonNull;

public interface MessageChangeListener {
    void onChange(long mid, @NonNull IMMessage message);

    void onInsertMessage(long mid, @NonNull IMMessage message);

    void onNewMessage(@NonNull IMMessage message);
}
