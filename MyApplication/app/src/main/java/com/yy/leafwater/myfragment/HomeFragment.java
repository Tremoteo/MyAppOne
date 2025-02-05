package com.yy.leafwater.myfragment;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.wch.multiport.MultiPortManager;
import com.yy.leafwater.MyApplication;
import com.yy.leafwater.MyOperate;
import com.yy.leafwater.R;


public class HomeFragment extends Fragment implements View.OnClickListener {
    private View homeView;

    private Button btn_findme;
    private Button btn_maptype;
    private ToggleButton btn_SelectAll;

    private MyApplication myApplication;

    private MyOperate myOperate = null;
    private MyOperate.MySensorUpdateThread mySensorUpdateThread = null;
    private MultiPortManager multipart = null;

    private static final String ACTION_USB_PERMISSION = "com.yy.leafwater.USB_PERMISSION";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (homeView == null) {
            homeView = inflater.inflate(R.layout.fragment_home, container, false);

            //初始化控件
            initViews();

            //定位
            myApplication = (MyApplication) requireActivity().getApplication();
            myApplication.myBaiduMap.startMap(requireActivity(), homeView.findViewById(R.id.bmapView), homeView.findViewById(R.id.tv_Lat), homeView.findViewById(R.id.tv_Lon), homeView.findViewById(R.id.tv_Add));

            //设备
            multipart = new MultiPortManager(
                    (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE), this.requireContext(),
                    ACTION_USB_PERMISSION);
            myOperate = new MyOperate(requireContext(), multipart, homeView);
            mySensorUpdateThread = myOperate.new MySensorUpdateThread();
        } else {
            ViewGroup parent = (ViewGroup) homeView.getParent();
            if (null != parent) {
                parent.removeView(homeView);
            }
        }

        return homeView;
    }

    @Override
    public void onResume() {
        myApplication.myBaiduMap.onResume();
        super.onResume();
        if (2 == multipart.ResumeUsbList()) {
            multipart.CloseDevice();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        myApplication.myBaiduMap.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (multipart != null) {
            if (multipart.isConnected()) {
                multipart.CloseDevice();
            }
            multipart = null;
        }
        myApplication.myBaiduMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_Init) {
            myOperate.initDevice();
        } else if (v.getId() == R.id.btn_OnOff) {
            if (mySensorUpdateThread.isAlive()) {
                mySensorUpdateThread.flagChange();
            } else {
                mySensorUpdateThread = myOperate.new MySensorUpdateThread();
                mySensorUpdateThread.flagChange();
                mySensorUpdateThread.start();
            }
        } else if (v.getId() == R.id.btn_Clean) {
            myOperate.myClean();
        } else if (v.getId() == R.id.btn_findme) {
            myApplication.myBaiduMap.but_findme = (myApplication.myBaiduMap.but_findme + 1) % 2;
            if (myApplication.myBaiduMap.but_findme == 0) {
                btn_findme.setBackground(AppCompatResources.getDrawable(this.requireContext(), R.drawable.baseline_signal_wifi_4_bar_24));
            } else {
                btn_findme.setBackground(AppCompatResources.getDrawable(this.requireContext(), R.drawable.baseline_center_focus_strong_24));
            }
            myApplication.myBaiduMap.findMe();
        } else if (v.getId() == R.id.btn_maptype) {
            myApplication.myBaiduMap.but_maptype = (myApplication.myBaiduMap.but_maptype + 1) % 2;
            if (myApplication.myBaiduMap.but_maptype == 0) {
                btn_maptype.setBackground(AppCompatResources.getDrawable(this.requireContext(), R.drawable.baseline_map_24));
            } else {
                btn_maptype.setBackground(AppCompatResources.getDrawable(this.requireContext(), R.drawable.baseline_satellite_alt_24));
            }
            myApplication.myBaiduMap.changeMapType();
        } else if (v.getId() == R.id.btn_SelectAll) {
            if (btn_SelectAll.isChecked()) {
                myOperate.myRecyclerViewAdapter.selectAll();
            } else {
                myOperate.myRecyclerViewAdapter.unselectAll();
            }
        } else if (v.getId() == R.id.btn_Delete) {
            myOperate.myRecyclerViewAdapter.deleteSelected();
        } else if (v.getId() == R.id.btn_Exit) {
            btn_SelectAll.setChecked(false);
            myOperate.myRecyclerViewAdapter.unselectAll();
            myOperate.myRecyclerViewAdapter.setSelectMode(false);
        }
    }

    private void initViews() {
        btn_findme = homeView.findViewById(R.id.btn_findme);
        btn_maptype = homeView.findViewById(R.id.btn_maptype);
        Button bt_Init = homeView.findViewById(R.id.btn_Init);
        ToggleButton bt_OnOff = homeView.findViewById(R.id.btn_OnOff);
        Button bt_Clean = homeView.findViewById(R.id.btn_Clean);

        btn_findme.setOnClickListener(this);
        btn_maptype.setOnClickListener(this);
        bt_Init.setOnClickListener(this);
        bt_OnOff.setOnClickListener(this);
        bt_Clean.setOnClickListener(this);

        //隐藏/显示的控件
        btn_SelectAll = homeView.findViewById(R.id.btn_SelectAll);
        Button btn_Delete = homeView.findViewById(R.id.btn_Delete);
        Button btn_Exit = homeView.findViewById(R.id.btn_Exit);

        btn_SelectAll.setOnClickListener(this);
        btn_Delete.setOnClickListener(this);
        btn_Exit.setOnClickListener(this);
    }

}