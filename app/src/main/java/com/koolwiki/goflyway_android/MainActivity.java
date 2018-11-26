package com.koolwiki.goflyway_android;


import com.koolwiki.goflyway_android.service.WhiteService;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.otto.Subscribe;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.koolwiki.goflyway_android.CmdParam.ACL;


public class MainActivity extends AppCompatActivity implements Constants{

    private static final String TAG = "MainActivity";
    private String goflyway = "goflyway";
    private String chinalist = "chinalist.txt";
    private String binary_path = null;
    private String chinalist_path = null;
    private int count_acl = 0;
    @InjectView(R.id.info)
    TextView mInfo;
    @InjectView(R.id.settingBtn)
    Button mSettingBtn;
    @InjectView(R.id.startBtn)
    Button mStartBtn;
    private CmdParam cmdParam = null;
    private Process process = null;
    private SharedPreferences mSharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        BusProvider.getInstance().register(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tryToStart();
        Intent whiteIntent = new Intent(getApplicationContext(), WhiteService.class);
        startService(whiteIntent);
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
        Boolean settingChanged = false;
        settingChanged = mSharedPreferences.getBoolean(SettingChanged, false);
        if (settingChanged){
            if (process != null) {
                killgoflyway();
            }
            tryToStart();
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(SettingChanged, false);
            editor.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killgoflyway();
        BusProvider.getInstance().unregister(this);
    }

    private void tryToStart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cmdParam = new CmdParam();
        //cmdParam.A = "chinalist";
        cmdParam.localaddr = "0.0.0.0:" + sharedPreferences.getString(LocalServerPort, "");
        cmdParam.remoteaddr = sharedPreferences.getString(RemoteServerIp, "").equalsIgnoreCase("") ? "": sharedPreferences.getString(RemoteServerIp, "") + ":" + sharedPreferences.getString(RemoteServerPort,"");
        cmdParam.key = sharedPreferences.getString(ServerKey, "");
        cmdParam.U = sharedPreferences.getString(UnderLay, "");
        if (cmdParam.isBasicOk()){
            handleStartBtnClick();
        }else {
            cmdParam = null;
            if (BuildConfig.DEBUG) Log.d(TAG, "need more info ");
            Toast.makeText(this, getString(R.string.no_setting_info), Toast.LENGTH_SHORT).show();
            BusProvider.getInstance().post(new MessageEvent(getString(R.string.no_setting_info), SET_INFO_CONTENT));
        }
    }

    @OnClick({R.id.settingBtn, R.id.startBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settingBtn:
                handleSettingBtnClick();
                break;
            case R.id.startBtn:
                tryToStart();
                break;
        }
    }

    private void handleSettingBtnClick() {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
    }

    private void handleStartBtnClick() {
        if (mStartBtn.getText().toString().equalsIgnoreCase(getString(R.string.start))){
            BusProvider.getInstance().post(new MessageEvent("", SET_INFO_CONTENT));
            String arch = System.getProperty("os.arch");
            if (BuildConfig.DEBUG) Log.d(TAG, arch);
            BusProvider.getInstance().post(new MessageEvent(getString(R.string.arch_info)+arch, APPEND_INFO_CONTENT));
            int identifier = detectCpuArchInfo();
            if (identifier == 0){
                Toast.makeText(this, R.string.detect_cpu_info_error, Toast.LENGTH_SHORT).show();
                return;
            }
            BusProvider.getInstance().post(new MessageEvent(getString(R.string.stop),CHANGE_START_BTN_NAME));
            BusProvider.getInstance().post(new MessageEvent(false,CHANGE_SETTING_BTN_ENABLE));
            binary_path = installBinary(this, identifier, goflyway);
            chinalist_path = installList(this);

//            BusProvider.getInstance().post(new MessageEvent(binary_path, APPEND_INFO_CONTENT));
            if (BuildConfig.DEBUG) Log.d(TAG, binary_path);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    runCmdLine(new ShellCallback() {
                        @Override
                        public void shellOut(String shellLine) {
                            BusProvider.getInstance().post(new MessageEvent(shellLine, APPEND_INFO_CONTENT));
                        }

                        @Override
                        public void processComplete(int exitValue) {
                            if (BuildConfig.DEBUG) Log.d(TAG, "exitValue:" + exitValue);
                            //restore
                            BusProvider.getInstance().post(new MessageEvent(getString(R.string.start),CHANGE_START_BTN_NAME));
                            BusProvider.getInstance().post(new MessageEvent(true,CHANGE_SETTING_BTN_ENABLE));
                        }
                    });
                }
            }).start();
        }else if (mStartBtn.getText().toString().equalsIgnoreCase(getString(R.string.stop))) {
            killgoflyway();
            BusProvider.getInstance().post(new MessageEvent(getString(R.string.start),CHANGE_START_BTN_NAME));
            BusProvider.getInstance().post(new MessageEvent(true,CHANGE_SETTING_BTN_ENABLE));
        }
    }

    /**
     * 根据CPU指令集的不同，返回不同的可执行程序ID
     *
     * @return
     */
    private int detectCpuArchInfo() {
        String arch = System.getProperty("os.arch");
        int identifierId = 0;
        arch = arch == null ? "" : arch;
        if (arch.contains("arm")) {
            if (arch.contains("v7")) {
                identifierId = getResources().getIdentifier("goflyway", "raw", getPackageName());
            } else if (arch.contains("v6")) {
                identifierId = getResources().getIdentifier("goflyway", "raw", getPackageName());
            } else if (arch.contains("v5")) {
                identifierId = getResources().getIdentifier("goflyway", "raw", getPackageName());
            }
        }else if (arch.contains("aarch64")) {
            //目前采取兼容模式
            identifierId = getResources().getIdentifier("goflyway", "raw", getPackageName());
        }else if (arch.contains("x86_64")) {
            //目前采取兼容模式
            identifierId = getResources().getIdentifier("goflyway", "raw", getPackageName());
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "identifierId:" + identifierId);
        return identifierId;
    }


    /**
     * 安装可执行程序
     *
     * @param ctx
     * @param resId
     * @param filename
     * @return
     */
    private  String installBinary(Context ctx, int resId, String filename) {
        try {
            File f = new File(ctx.getApplicationContext().getFilesDir().getAbsolutePath(), filename);
            if (f.exists()) {
                handleKcptunUpdate(ctx, resId, f);
            } else {
                copyRawFile(ctx,resId,f,"0755");
                Log.d(TAG, f + " installed(copy)");
            }
            return f.getCanonicalPath();
        } catch (Exception e) {
            Log.e(TAG, "installBinary failed: " + e.getLocalizedMessage());
            return null;
        }
    }


    private void handleKcptunUpdate(Context ctx, int resId, File f) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        int currentVersionCode = pInfo.versionCode;
        int localVersionCode = mSharedPreferences.getInt(AppVersionCode,0);
        if (localVersionCode < currentVersionCode) {
            copyRawFile(ctx,resId,f,"0755");
            Log.d(TAG, f + " installed(handle)");
        }
        else{
            if (BuildConfig.DEBUG) Log.d(TAG, f + " not updated");
        }
    }

    private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException {
        final String abspath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        final InputStream is = ctx.getResources().openRawResource(resid);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }



    private  String installList(Context ctx) {
        try {
            File f = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/chinalist.txt");
            if (f.exists()) {
                handleListUpdate(f);
            } else {
                //handleListUpdate(f);
                copyListFile(f,"0644");
                Log.d(TAG, f + " installed(copy)");
             //   Log.d(TAG, f + " exist: " );
            }
            return f.getCanonicalPath();
        } catch (Exception e) {
            Log.e(TAG, "installList failed: " + e.getLocalizedMessage());
            return null;
        }
    }


    private void handleListUpdate(File f) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        int currentVersionCode = pInfo.versionCode;
        int localVersionCode = mSharedPreferences.getInt(AppVersionCode,0);
        if (localVersionCode < currentVersionCode) {
            copyListFile(f,"0644");
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(AppVersionCode,currentVersionCode);
            editor.commit();
            Log.d(TAG, f + " installed(handle)");
        }
        else{
            if (BuildConfig.DEBUG) Log.d(TAG, f + " not updated");
        }
    }

    private  void copyListFile(File file, String mode) throws IOException, InterruptedException {
        final String abspath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        final InputStream is = getResources().openRawResource(R.raw.chinalist);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }



    /**
     * 执行指令
     *
     * @param sc
     * @return
     */
    private int runCmdLine(ShellCallback sc) {
        // Executes the command.



        if (cmdParam == null){
//            Toast.makeText(this, "Please Fill The Setting First", Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG) Log.d(TAG, "Please Fill The Setting First");
            return -1;
        }
        if (binary_path == null) {
            return -1;
        }

        final String cmd = setup_cmd(binary_path, cmdParam);

        BusProvider.getInstance().post(new MessageEvent(cmd,APPEND_INFO_CONTENT));

        process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(
                process.getErrorStream(), "ERROR", sc);

        // any output?
        StreamGobbler outputGobbler = new
                StreamGobbler(process.getInputStream(), "OUTPUT", sc);

        errorGobbler.start();
        outputGobbler.start();

        int exitVal = 0;
        try {
            exitVal = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sc.processComplete(exitVal);

        return exitVal;
    }



    private String setup_cmd(String binary_path, CmdParam cmdParam) {

        ArrayList<String> params = new ArrayList<>();
        if (cmdParam.localaddr != null) {
            if(cmdParam.localaddr.indexOf("-acl") != -1)
            {
                count_acl = count_acl + 1;
            }
            params.add(CmdParam.LOCALADDR);
            params.add(cmdParam.localaddr);
        }

        if (cmdParam.remoteaddr != null) {
            if(cmdParam.remoteaddr.indexOf("-acl") != -1)
            {
                count_acl = count_acl + 1;
            }
            params.add(CmdParam.REMOTEADDR);
            params.add(cmdParam.remoteaddr);
        }

        if (cmdParam.key != null) {
            if(cmdParam.key.indexOf(" -acl") != -1)
            {
                count_acl = count_acl + 1;
            }
            params.add(CmdParam.KEY);
            params.add(cmdParam.key);
        }

        if (cmdParam.U != null) {
            if(cmdParam.U.indexOf("-acl") != -1)
            {
                count_acl = count_acl + 1;
            }
        }

        if (count_acl != 0) {
            params.add(CmdParam.UNDERLAY);
            params.add(cmdParam.U);
            Log.d(TAG, "count_acl!=" + count_acl );
            count_acl = 0;
        }

        else  {
            params.add(CmdParam.UNDERLAY);
            params.add(cmdParam.U + " " + ACL + " " + chinalist_path);
            Log.d(TAG, "count_acl==" + count_acl );

        }
        StringBuilder stringBuilder = new StringBuilder(binary_path);
        for (String param : params) {
            stringBuilder.append(' ');
            stringBuilder.append(param);
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }


    private void killgoflyway(){
        if (process != null){
            process.destroy();
            try {
                int retVal = process.waitFor();
                if (BuildConfig.DEBUG) Log.d(TAG, "retVal:" + retVal);
                if (retVal == 9) {
                    BusProvider.getInstance().post(new MessageEvent(getString(R.string.stop_kcp_tun_normal),SET_INFO_CONTENT));
                }else{
                    BusProvider.getInstance().post(new MessageEvent(getString(R.string.stop_kcp_tun_exception),SET_INFO_CONTENT));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void handleInfoContentChange(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getType() == SET_INFO_CONTENT) {
                    mInfo.setText(messageEvent.getMsg());
                }else if (messageEvent.getType() == APPEND_INFO_CONTENT){
                    mInfo.append(messageEvent.getMsg()+"\n");
                }
            }
        });
    }

    @Subscribe
    public void handleBtnName(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getType() == CHANGE_START_BTN_NAME) {
                    mStartBtn.setText(messageEvent.getMsg());
                }
            }
        });
    }

    @Subscribe
    public void handleBtnEnable(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getType() == CHANGE_START_BTN_ENABLE) {
                    mStartBtn.setEnabled(messageEvent.isEnable());
                }else if (messageEvent.getType() == CHANGE_SETTING_BTN_ENABLE) {
                    mSettingBtn.setEnabled(messageEvent.isEnable());
                }
            }
        });
    }


}
