package com.yy.leafwater;

import static com.wch.multiport.MultiPortManager.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yy.leafwater.mydb.MyDBEngine;
import com.yy.leafwater.myfragment.DatabaseFragment;
import com.yy.leafwater.myfragment.HomeFragment;
import com.yy.leafwater.myfragment.SettingFragment;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_home;
    private Button btn_database;
    private Button btn_setting;
    private MyDBEngine myDBEngine;

    private long mBackPressed;
    private static final int TIME_EXIT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);  // 隐藏标题栏
        setContentView(R.layout.activity_main);

        myDBEngine = MyDBEngine.getINSTANCE(this.getApplicationContext());
        initViews();

        int test = 61238;
        Log.e(TAG, "onCreate: " + (test >> 10));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDBEngine.shutDown();
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_EXIT > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "再点击一次返回退出程序", Toast.LENGTH_SHORT).show();
            mBackPressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_home) {
            btn_home.setEnabled(false);
            btn_database.setEnabled(true);
            btn_setting.setEnabled(true);
            btn_home.getBackground().setTint(getResources().getColor(R.color.purple_700, null));
            btn_database.getBackground().setTint(getResources().getColor(R.color.gray, null));
            btn_setting.getBackground().setTint(getResources().getColor(R.color.gray, null));
            replaceConstraintlayout(new HomeFragment());
        } else if (v.getId() == R.id.btn_database) {
            btn_home.setEnabled(true);
            btn_database.setEnabled(false);
            btn_setting.setEnabled(true);
            btn_home.getBackground().setTint(getResources().getColor(R.color.gray, null));
            btn_database.getBackground().setTint(getResources().getColor(R.color.purple_700, null));
            btn_setting.getBackground().setTint(getResources().getColor(R.color.gray, null));
            replaceConstraintlayout(new DatabaseFragment());
        } else if (v.getId() == R.id.btn_setting) {
            btn_home.setEnabled(true);
            btn_database.setEnabled(true);
            btn_setting.setEnabled(false);
            btn_home.getBackground().setTint(getResources().getColor(R.color.gray, null));
            btn_database.getBackground().setTint(getResources().getColor(R.color.gray, null));
            btn_setting.getBackground().setTint(getResources().getColor(R.color.purple_700, null));
            replaceConstraintlayout(new SettingFragment());
        }
    }

    private void initViews() {
        btn_home = findViewById(R.id.btn_home);
        btn_database = findViewById(R.id.btn_database);
        btn_setting = findViewById(R.id.btn_setting);

        btn_home.setOnClickListener(this);
        btn_database.setOnClickListener(this);
        btn_setting.setOnClickListener(this);

        btn_home.setEnabled(false);
        btn_database.setEnabled(true);
        btn_setting.setEnabled(true);
        btn_home.getBackground().setTint(getResources().getColor(R.color.purple_700, null));
        btn_database.getBackground().setTint(getResources().getColor(R.color.gray, null));
        btn_setting.getBackground().setTint(getResources().getColor(R.color.gray, null));

        firstRun();

        replaceConstraintlayout(new HomeFragment());
    }

    private void replaceConstraintlayout(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.constraintlayout1, fragment);
        transaction.commit();
    }

    private void firstRun() {
        // 判断是否是首次运行
        SharedPreferences sharedPreferences = this.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("is_first_run", true);

        if (isFirstRun) {
            // 首次运行存储is_first_run为false
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_first_run", false);
            editor.apply();
            // 首次运行，保存初始配置数据到SharedPreferences
            StringBuilder stringBuilder = new StringBuilder();
            for (long value : MyListInfo.REG) {
                stringBuilder.append(value).append(",");
            }
            String arrayString = stringBuilder.toString();
            editor.putString("REG", arrayString);
            editor.apply();
        }
    }

}