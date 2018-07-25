package com.example.minato.minastore.down;

/**
 * Created by minato on 2018/7/20.
 * 文件下载的状态
 */

public enum  DownState {
    START(0),
    DOWNING(1),
    PAUSE(2),
    STOP(3),
    ERROR(4),
    FINISH(5);
    private int mState;

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        this.mState = state;
    }

    DownState(int state) {
        this.mState = state;
    }
}
