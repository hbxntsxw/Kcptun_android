package com.koolwiki.goflyway_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingActivity extends AppCompatActivity implements Constants{

    @InjectView(R.id.remote_server_ip)
    TextInputEditText mRemoteServerIp;
    @InjectView(R.id.remote_server_ip_wrapper)
    TextInputLayout mRemoteServerIpWrapper;
    @InjectView(R.id.remote_server_port)
    TextInputEditText mRemoteServerPort;
    @InjectView(R.id.remote_server_port_wrapper)
    TextInputLayout mRemoteServerPortWrapper;
    @InjectView(R.id.local_server_port)
    TextInputEditText mLocalServerPort;
    @InjectView(R.id.local_server_port_wrapper)
    TextInputLayout mLocalServerPortWrapper;
    @InjectView(R.id.remote_server_key)
    TextInputEditText mRemoteServerKey;
    @InjectView(R.id.remote_server_key_wrapper)
    TextInputLayout mRemoteServerKeyWrapper;
    @InjectView(R.id.server_mode)
    TextInputEditText mServerMode;
    @InjectView(R.id.server_mode_wrapper)
    TextInputLayout mServerModeWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.inject(this);
        loadOldContent();
    }

    private void loadOldContent() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        handleRestore(sharedPreferences, mRemoteServerIp, RemoteServerIp);
        handleRestore(sharedPreferences, mRemoteServerPort, RemoteServerPort);
        handleRestore(sharedPreferences, mLocalServerPort, LocalServerPort);
        handleRestore(sharedPreferences, mRemoteServerKey, ServerKey);
        handleRestore(sharedPreferences, mServerMode, ServerMode);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleContentChange();
    }

    private void handleRestore(SharedPreferences sharedPreferences,TextInputEditText textInputEditText, String key) {
        String oldStr = sharedPreferences.getString(key,"");
        textInputEditText.setText(oldStr);
    }

    private void handleContentChange(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        handleChange(sharedPreferences, mRemoteServerIp, RemoteServerIp);
        handleChange(sharedPreferences, mRemoteServerPort, RemoteServerPort);
        handleChange(sharedPreferences, mLocalServerPort, LocalServerPort);
        handleChange(sharedPreferences, mRemoteServerKey, ServerKey);
        handleChange(sharedPreferences, mServerMode, ServerMode);
    }

    private void handleChange(SharedPreferences sharedPreferences,TextInputEditText textInputEditText, String key){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String newStr = textInputEditText.getText().toString().trim();
        String oldStr = sharedPreferences.getString(key,"");
        if (!newStr.contentEquals(oldStr)){
            editor.putString(key, newStr);
            editor.putBoolean(SettingChanged,true);
            editor.commit();
        }
    }
}
