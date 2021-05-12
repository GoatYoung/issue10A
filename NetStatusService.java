package com.young.project02.service;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
public class NetStatusService extends Service {
    private ConnectivityManager connectivityManager;
    private boolean connecting;
    private boolean isNetConn = true;
    private boolean lastNetStatus = true;
    private Callback callback;
    @Override
    public void onCreate() {
        super.onCreate();
        connecting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(connecting==true){
                    if(callback != null){
                        if(isNetworkConnected(NetStatusService.this)){
                            isNetConn = true;
                        }else{
                            isNetConn = false;
                        }
                        //如果状态发生变化，则触发回调函数
                        if(isNetConn!=lastNetStatus){
                            callback.onNetworkChange(isNetConn);
                            lastNetStatus = isNetConn;
                        }
                    }
                }
            }
        }).start();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        connecting = false;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public class MyBinder extends Binder {
        public NetStatusService getService(){
            return NetStatusService.this;
        }
    }

    public static interface Callback{
        void onNetworkChange(boolean isConn);
    }
}
