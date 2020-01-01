package com.sonydafa.screenTime;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Page1Fragment extends Fragment {
    private View view;
    private Context context;
    @Override
    public View onCreateView(LayoutInflater  inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.page1, container, false);
        return view;
    }
    @Override
    public void onStart() {
        long start =new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).getMillis();
        long end=System.currentTimeMillis();
        refreshPage(start,end);
        Log.d("homePage","第一个view");
        setWeekChart();
        super.onStart();
    }
    private void setWeekChart(){
        BarChart barChart = getView().findViewById(R.id.weekChart);
        int[] weekStatic = Tools.getWeekScreenTime(context);
        initChart(barChart,weekStatic);
    }
    Page1Fragment(Context context){
        this.context=context;
    }
    private void refreshPage(long start,long end){
        ArrayList<HashMap<String, Object>> dataSet= new ArrayList<>();
        int defaultIcon=R.drawable.android;
        Context context = getContext();
        int totalTimeUsage=0;
        PackageManager pm = context.getPackageManager();
        List<App> apps = Tools.getApps(context, start, end);
        for (App appUsage:apps){
            HashMap<String,Object>pairs=new HashMap<>();
            if(appUsage.getFrontTime()==0) continue;
            totalTimeUsage+=appUsage.getFrontTime();
            if(appUsage.getRealName()!=null && !appUsage.getRealName().equals("")){
                pairs.put("title",appUsage.getRealName());
                pairs.put("spanTime",Tools.sec2hourMin(appUsage.getFrontTime()));
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(appUsage.getPackageName(), PackageManager.GET_META_DATA);
                    Drawable drawable = appInfo.loadIcon(pm);
                    pairs.put("picture",drawable);
                }catch(PackageManager.NameNotFoundException e){
                    e.printStackTrace();
                }
            }
            else{
                pairs.put("title", "已卸载的应用");
                pairs.put("spanTime",Tools.sec2hourMin(appUsage.getFrontTime()));
                //默认已经卸载过的icon
                pairs.put("picture",defaultIcon);
            }
            dataSet.add(pairs);
        }
        HashMap<String,Object>pairs=new HashMap<>();
        pairs.put("title","Screen");
        pairs.put("picture",R.drawable.screen);
        pairs.put("spanTime",Tools.sec2hourMin(totalTimeUsage));
        dataSet.add(0,pairs);
        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), dataSet, R.layout.photo_item, new String[]{"picture", "title","spanTime"}, new int[]{R.id.image, R.id.title, R.id.timeSpan});
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String s) {
                if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView iv = (ImageView) view;
                    iv.setImageDrawable((Drawable) data);
                    return true;
                } else
                    return false;
            }
        });
        ListView listView=getView().findViewById(R.id.list_view);
        listView.setAdapter(simpleAdapter);;
        //super.onStart();
    }

    private static  float mill2hour(int mill){
        return (float) mill/(60*60);
    }
    //init barChart
    private  void initChart(BarChart barChart, int[]weekStatic){
        ArrayList<BarEntry> yValues=new ArrayList<>();
        Log.i("chart", Arrays.toString(weekStatic));
        int len=weekStatic.length;
        DateTime dt=new DateTime();
        for(int i=len-1;i>=0;i--){
            yValues.add(new BarEntry(len-1-i,mill2hour(weekStatic[i])));
        }
        String[]str=new String[weekStatic.length];
        for(int i=0;i<len;i++){
            DateTime dateTime = dt.minusDays(len-1-i);
            String dateStr=dateTime.getMonthOfYear()+"/"+dateTime.getDayOfMonth();
            str[i]=dateStr;
        }
        //设置X轴为字符串
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new XAxisValueFormatter(str));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //y axis
        barChart.getAxisRight().setEnabled(false);;
        //从0刻度开始
        barChart.getAxisLeft().setStartAtZero(true);
        BarDataSet barDataSet=new BarDataSet(yValues,"week");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        Description description=new Description();
        description.setText("a week screen time");
        barChart.setDescription(description);;
        //禁止缩放
        barChart.setScaleEnabled(false);
        //
        barChart.getXAxis().setDrawGridLines(false);
        //根据点击的Bar,切换应用时长详情
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                float x=entry.getX();
                int xi=6-(int)x;
                Log.i("chart_define","点击了x:"+x+"块");

                DateTime base=new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                long start = base.minusDays(xi).getMillis();
                long end=base.minusDays(xi-1).getMillis();
                if(xi==0)
                    end=System.currentTimeMillis();
                refreshPage(start,end);
            }
            @Override
            public void onNothingSelected() {

            }
        });
        barChart.invalidate();
    }
}
