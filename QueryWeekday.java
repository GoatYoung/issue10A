package com.young.project02.service;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.young.project02.IMyAidlInterface;

import java.util.Calendar;

public class QueryWeekday extends Service {
    private final IBinder mBinder = new IMyAidlInterface.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String remoteGetWeekday(String date) throws RemoteException {
            return getWeekday(date);
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
//        return new MyBinder();
        return mBinder;
    }
    public class MyBinder extends Binder{
        public QueryWeekday getService(){
            return QueryWeekday.this;
        }
    }
    public String getWeekday(String date){
        String[] dates = date.split("-");
        String[] weekdays = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六",};
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,Integer.parseInt(dates[0]));
        cal.set(Calendar.MONTH,Integer.parseInt(dates[1])-1);
        cal.set(Calendar.DATE,Integer.parseInt(dates[2]));
        int weekday = cal.get(Calendar.DAY_OF_WEEK)-1;
        return date+"的那天是"+weekdays[weekday];
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}