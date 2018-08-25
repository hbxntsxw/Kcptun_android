package com.koolwiki.goflyway_android;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by shutup on 16/7/20.
 */
public class StreamGobbler extends Thread{
    private static final String TAG = "StreamGobbler";
    InputStream is;
    String type;
    ShellCallback sc;

    StreamGobbler(InputStream is, String type, ShellCallback sc) {
        this.is = is;
        this.type = type;
        this.sc = sc;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (sc != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, line);
                    sc.shellOut(line);
                }
            }
        } catch (IOException ioe) {
            //   Log.e(TAG,"error reading shell slog",ioe);
            ioe.printStackTrace();
        }
    }
}
