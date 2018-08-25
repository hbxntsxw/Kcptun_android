package com.koolwiki.goflyway_android;

/**
 * Created by shutup on 16/7/20.
 */
public interface ShellCallback {
    public void shellOut(String shellLine);

    public void processComplete(int exitValue);
}
