package com.koolwiki.goflyway_android;

/**
 * Created by shutup on 16/7/23.
 */
public class MessageEvent {
    private String msg;
    private int type;
    private boolean isEnable;

    public MessageEvent(String msg, int type) {
        this.msg = msg;
        this.type = type;
    }

    public MessageEvent( boolean isEnable,int type) {
        this.type = type;
        this.isEnable = isEnable;
    }

    public String getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }

    public boolean isEnable() {
        return isEnable;
    }
}
