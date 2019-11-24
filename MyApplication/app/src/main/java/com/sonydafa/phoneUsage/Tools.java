package com.sonydafa.phoneUsage;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class Tools {
    //保存的数据集合
    private static List<App> dataList=new LinkedList<>();
    public static List<App> getDataSet() {
        return dataList;
    }
    //格式化函数,将s的单位，换成H M S的格式
    public static String sec2hourMin(int second){
        if(second<60) return second+" s";
        else if(second>60 && second<60*60) return second/60+" m "+second%60+" s";
        else{
            int h=second/3600;
            second=second%3600;
            int min=second/60;
            return h+" h "+min+" m "+second%60+" s";
        }
    }
    //获取一周的数据，数据格式:"今天时长,第1天前时长，第2天前时长，...,第6天前时长"
    public static int[] getWeekScreenTime(Context context){
        UsageStatsManager usm=(UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        DateTime today =new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        long current=System.currentTimeMillis();
        if(usm==null){
            Log.e("fatal","usm(UsageStatsManger) is null");
            return null;
        }
        UsageEvents usageEvents = usm.queryEvents(today.getMillis(),current);
        int []weeks=new int[7];
        weeks[0]=getScreenTime(usageEvents);
        //获取前6天
        for(int i=1;i<7;i++){
            long begin=today.minusDays(i).getMillis();
            long end=today.minusDays(i-1).getMillis();
            usageEvents=usm.queryEvents(begin,end);
            weeks[i]=getScreenTime(usageEvents);
        }
        return weeks;
    }
    //给定一个usageEvents，返回总亮屏时常,通常调用是00:00-23:59
    private static int getScreenTime(UsageEvents usageEvents){
        long sum=0;
        Map<String,Long> openTime=new HashMap<>();
        while (usageEvents.hasNextEvent()){
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            String packageName;
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    packageName = event.getPackageName();
                    openTime.put(packageName,event.getTimeStamp());
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    packageName = event.getPackageName();
                    long closeTimeStamp=event.getTimeStamp();
                    long openTimeStamp = closeTimeStamp;
                    if(openTime.containsKey(packageName))
                        openTimeStamp=openTime.get(packageName);
                    int diff=(int)(closeTimeStamp-openTimeStamp);
                    sum += diff;
                    break;
            }
        }
        return (int)(sum/1000);
    }
    //return instance of App,which include AppName,package name, during time
    public static List<App> getApps(Context context, long start, long end){
        //已经统计过了，重新统计
        if(dataList.size()!=0)
dataList.clear();
        Map<String,Integer> allApp=inner_getAllAppUseTime(context,start,end);
        PackageManager pm = context.getPackageManager();
        if(allApp==null) return null;
        for(Map.Entry<String,Integer> entry:allApp.entrySet()){
            int time=entry.getValue()/(1000);
            String packageName=entry.getKey();
            String appRealName="";
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                appRealName=pm.getApplicationLabel(appInfo).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            dataList.add(new App(packageName,appRealName,time));
        }
        Collections.sort(dataList);
        return dataList;
    }
    //私有方法,返回 {"Package-Name":AppTime},单位是毫秒ms
    private static Map<String,Integer> inner_getAllAppUseTime(Context context,long start,long end){
        UsageStatsManager usm=(UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if(usm==null){
            Log.e("fatal","usm(UsageStatsManger) is null");
            return null;
        }
        UsageEvents usageEvents = usm.queryEvents(start, end);
        Map<String,Long> openTime=new HashMap<>();
        Map<String,Integer> allApp=new HashMap<>();
        while (usageEvents.hasNextEvent()){
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            String packageName=event.getPackageName();
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    openTime.put(packageName,event.getTimeStamp());
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    long closeTimeStamp=event.getTimeStamp();
                    long openTimeStamp = closeTimeStamp;
                    if(openTime.containsKey(packageName))
                        openTimeStamp=openTime.get(packageName);
                    int diff=(int)(closeTimeStamp-openTimeStamp);
                    int thisAppTotalTime=allApp.get(packageName)!=null?allApp.get(packageName):0;
                    allApp.put(packageName,thisAppTotalTime+diff);
                    break;
            }
        }
         return allApp;
    }
}
