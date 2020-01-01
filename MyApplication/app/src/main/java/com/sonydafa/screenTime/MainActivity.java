package com.sonydafa.screenTime;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private FragmentTransaction transaction;
    private FragmentManager fragmentManager;
    Fragment []fragments;
    //设置碎片化界面
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            fragmentManager = getSupportFragmentManager();
            transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //transaction.replace(R.id.container, new Page1Fragment(getApplicationContext()));
                    transaction.hide(fragments[1]);
                    transaction.hide(fragments[2]);
                    transaction.show(fragments[0]);
                    transaction.commit();
                    Log.i("demo","page1");
                    return true;
                case R.id.navigation_dashboard:
                    //transaction.replace(R.id.container, new Page2Fragment());
                    transaction.hide(fragments[0]);
                    transaction.hide(fragments[2]);
                    transaction.show(fragments[1]);
                    transaction.commit();
                    Log.i("demo","page2");
                    return true;
                case R.id.navigation_notifications:
                    transaction.hide(fragments[0]);
                    transaction.hide(fragments[1]);
                    transaction.show(fragments[2]);
                    //transaction.replace(R.id.container, new Page3Fragment());
                    transaction.commit();
                    Log.i("demo","page3");
                    return true;
            }
            return false;
        }
    };
    private void setDefaultFragment() {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        Context applicationContext = getApplicationContext();
        Page1Fragment page1 = new Page1Fragment(getApplicationContext());
        Page2Fragment page2=new Page2Fragment();
        Page3Fragment page3=new Page3Fragment();
        fragments=new Fragment[3];
        fragments[0]=page1;
        fragments[1]=page2;
        fragments[2]=page3;
        //舒适化页面，将1,2,3都添加进去,但是只show(1)，hide(2),hide(3)，此后
        transaction.add(R.id.container,fragments[0]);
        transaction.add(R.id.container,fragments[1]);
        transaction.hide(fragments[1]);
        transaction.add(R.id.container,fragments[2]);
        transaction.hide(fragments[2]);
        transaction.show(fragments[0]);
        transaction.commit();
        //transaction.replace(R.id.container, new Page1Fragment(applicationContext)).commit();
        Log.i("demo","初始化页面完成");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //进行菜单管理
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation =findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //进行查看手机使用情况权限检查
        checkPhoneAuthority();
        //设置默认的fragment
        setDefaultFragment();
    }
    private void initAlterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置提示信息
        builder.setMessage("应用首次启动, 需要在[使用情况访问权限]中允许Screen Time查看使用情况, 否则应用无法执行");
        // 设置按钮
        builder.setPositiveButton("开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("start","用户开启设置允许本应用查看应用状态");
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        });
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("start","用户未授权，应用退出");
                System.exit(0);
            }
        });
        // 显示对话框（弹出）
        builder.show();
    }
    public void checkPhoneAuthority()
    {
        //判断是否具有权限
        boolean granted;
        //获取授权模块
        AppOpsManager appOps = (AppOpsManager)getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT)
            granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        else
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        if (!granted) {
            initAlterDialog();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
