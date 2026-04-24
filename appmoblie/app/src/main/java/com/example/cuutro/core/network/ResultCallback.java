package com.example.cuutro.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ResultCallback<T> {

    void onSuccess(@Nullable T data);

    void onError(@NonNull NetworkError error);
}
