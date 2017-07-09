package com.evangilo.jpromise;

public interface PromiseExecutor {
    void call(Function resolve, Function reject);
}
