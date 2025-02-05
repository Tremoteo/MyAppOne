package com.yy.leafwater.mybdmap;

import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.yy.leafwater.R;


public class MyBaiduMap {
    Context context;

    private MyOrientationListener myOrientationListener;//方向传感器
    private MapView mMapView;
    private LocationClient mLocationClient;//客户端
    private BaiduMap mBaiduMap;//地图控制器对象
    private TextView tv_Lat;  //纬度
    private TextView tv_Lon;  //经度
    private TextView tv_Add;  //地址
    public float mCurrentAccuracy;//当前定位精度
    private float mCurrentDirection;//当前方向
    public double mCurrentLat;//当前纬度
    public double mCurrentLon;//当前经度
    private boolean is_set = false;//setMap标志
    private boolean flag_MapLoaded = false;//地图加载完成标志
    private boolean flag_MapRender = false;//地图渲染完成标志
    public int but_findme = 1;//findme按钮状态
    public int but_maptype = 0;//maptype按钮状态


    public MyBaiduMap(Context context) {
        this.context = context;
        //设置隐私模式，默认true 注: 如果设置为false，一定要保证在调用 SDKInitializer.initialize(this)之前设置;
        SDKInitializer.setAgreePrivacy(context, true);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        try {
            SDKInitializer.initialize(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //设置使用的坐标类型，支持GCJ02和BD09LL两种坐标，默认是BD09LL坐标
        SDKInitializer.setCoordType(CoordType.GCJ02);
        //隐私合规接口
        LocationClient.setAgreePrivacy(true);
    }

    /**
     * 方向传感器
     */
    private class MyOrientationListener implements SensorEventListener {
        private final Context context;
        private SensorManager sensorManager;
        private Sensor magneticSensor, accelerometerSensor;

//        private Sensor rotationVectorSensor;

        private float[] gravity = new float[3];
        private float[] geomagnetic = new float[3];
        private double lastX;

        /**
         * 当有新的传感器事件时(手机方向改变时调用)调用。
         */
        @Override
        public void onSensorChanged(@NonNull SensorEvent event) {
            // SensorEvent:保存精度(accuracy)、传感器类型(sensor)、时间戳(timestamp)
            // 不同传感器(Sensor)具有的不同传感器数组(values)

//            // 处理传感器数据变化的事件
//            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//                float[] rotationMatrix = new float[9];
//                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

//                // 获取设备方向信息
//                float[] orientation = new float[3];
//                SensorManager.getOrientation(rotationMatrix, orientation);
//                mCurrentDirection = orientation[0];
//                MyLocationData locationData = new MyLocationData.Builder()
//                        .accuracy(mCurrentAccuracy)
//                        .direction(mCurrentDirection)
//                        .latitude(mCurrentLat)
//                        .longitude(mCurrentLon)
//                        .build();
//                mBaiduMap.setMyLocationData(locationData);//设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
//                 }


            // TYPE_MAGNETIC_FIELD:描述磁场传感器类型的常量。
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values;
            }
            // TYPE_ACCELEROMETER:描述加速度传感器类型的常量。
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values;
            }
            // 设置条件防止方向参数改变频繁
            if (gravity != null && geomagnetic != null) {
                float x = getValue();
                if (Math.abs(x - lastX) > 5) {
                    mCurrentDirection = 0;//-------------------------------------------------------------------------------------------------------------------
                    MyLocationData locationData = new MyLocationData.Builder()
                            .accuracy(mCurrentAccuracy)
                            .direction(mCurrentDirection)
                            .latitude(mCurrentLat)
                            .longitude(mCurrentLon)
                            .build();
                    mBaiduMap.setMyLocationData(locationData);//设置定位数据, 只有先允许定位图层后设置数据才会生效，参见 setMyLocationEnabled(boolean)
                    lastX = x;
                }
            }
        }

        /**
         * 当注册传感器的精度发生变化时调用。
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (mCurrentDirection == 1.0) {
                mCurrentDirection = (float) 1.0;
            }
        }

        private float getValue() { // 通过加速度和磁场变化获取方向变化的信息
            //初始化数组
            float[] values = new float[3]; // 用来保存手机的旋转弧度
            float[] r = new float[9]; // 被填充的旋转矩阵

            // 传入gravity和geomagnetic，通过计算它们得到旋转矩阵R。
            // 而第二个参数倾斜矩阵I是用于将磁场数据转换进实际的重力坐标系中的，一般默认设置为NULL即可。
            SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
            // 根据旋转矩阵R计算设备的方向，将结果存储在values中。
            // values[0]记录着手机围绕 Z 轴的旋转弧度，
            // values[1]记录着手机围绕 X 轴的旋转弧度，
            // values[2]记录着手机围绕 Y 轴的旋转弧度。
            SensorManager.getOrientation(r, values);
            // 旋转弧度转为角度
            return (float) Math.toDegrees(values[0]);
        }

        public MyOrientationListener(Context context) {
            this.context = context;
        }

        public void onStart() {
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

            if (sensorManager != null) { // 初始化两个传感器

//                // 获取旋转矢量传感器
//                rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//                // 注册旋转矢量传感器监听器
//                sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

                // getDefaultSensor:获取Sensor,使用给定的类型和唤醒属性返回传感器。
                magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            if (magneticSensor != null) {
                assert sensorManager != null;
                sensorManager.registerListener(this, magneticSensor,
                        SensorManager.SENSOR_DELAY_UI);
            }
            if (accelerometerSensor != null) {
                assert sensorManager != null;
                sensorManager.registerListener(this, accelerometerSensor,
                        SensorManager.SENSOR_DELAY_UI);
            }
        }

        public void onStop() {
            sensorManager.unregisterListener(this); // 传感器解除绑定
        }
    }

    /**
     * 定位请求回调
     */
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }

            mCurrentAccuracy = bdLocation.getRadius();
            mCurrentLat = bdLocation.getLatitude();
            mCurrentLon = bdLocation.getLongitude();

            //输出经纬度和地点
            tv_Add.setText(bdLocation.getAddrStr());
            tv_Lat.setText(String.valueOf(bdLocation.getLatitude()));
            tv_Lon.setText(String.valueOf(bdLocation.getLongitude()));
        }
    }

    /**
     * 初始化定位服务的客户端，定位参数配置
     */
    private void initLocationOption(Activity activity) {
        //从系统获取定位权限
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            try {
                //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
                mLocationClient = new LocationClient(activity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //配置定位参数
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            option.setCoorType("gcj02");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的;
            option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            option.setNeedDeviceDirect(true);//可选，在网络定位时，是否需要设备方向
            option.setLocationNotify(true);//可选，默认false，设置是否当卫星定位有效时按照1S1次频率输出卫星定位结果
            option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            option.setOpenGnss(true);//可选，默认false，设置是否开启卫星定位
            option.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
            option.setOpenAutoNotifyMode();//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
            option.setOpenAutoNotifyMode(1000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
            mLocationClient.setLocOption(option);
            //注册监听函数
            mLocationClient.registerLocationListener(new MyLocationListener());
            //启动定位服务的客户端
            // 使用 Handler 更新 UI 显示
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> mLocationClient.start());
        }
    }

    public void startMap(Activity activity, @NonNull MapView mMapView, TextView tv_Lat, TextView tv_Lon, TextView tv_Add) {
        this.tv_Lat = tv_Lat;
        this.tv_Lon = tv_Lon;
        this.tv_Add = tv_Add;
        //获取地图控件引用
        this.mMapView = mMapView;

        //开启位置传感器
        myOrientationListener = new MyOrientationListener(activity);
        myOrientationListener.onStart();
        //初始化定位服务的客户端，定位参数配置
        initLocationOption(activity);
        //mBaiduMap是地图控制器对象
        mBaiduMap = mMapView.getMap();

        //触摸地图回调接口
        BaiduMap.OnMapTouchListener listener = new BaiduMap.OnMapTouchListener() {
            /**
             * 当用户触摸地图时回调函数
             *
             * @param motionEvent 触摸事件
             */
            @Override
            public void onTouch(MotionEvent motionEvent) {
//                but_findme = 0;
                Button button = activity.findViewById(R.id.btn_findme);
                button.setBackground(AppCompatResources.getDrawable(activity, R.drawable.baseline_center_focus_weak_24));
                //配置定位图层显示方式
                MyLocationConfiguration mLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null, 0xAAFFFF88, 0xAA00FF00);
                mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);
            }
        };
        mBaiduMap.setOnMapTouchListener(listener);
        //地图加载完成回调
        BaiduMap.OnMapLoadedCallback callback = new BaiduMap.OnMapLoadedCallback() {
            /**
             * 地图加载完成回调函数
             */
            @Override
            public void onMapLoaded() {
                flag_MapLoaded = true;
            }
        };
        mBaiduMap.setOnMapLoadedCallback(callback);
        //地图渲染完成回调
        BaiduMap.OnMapRenderCallback callback1 = new BaiduMap.OnMapRenderCallback() {
            /**
             * 地图渲染完成回调函数
             */
            @Override
            public void onMapRenderFinished() {
                flag_MapRender = true;
            }
        };
        mBaiduMap.setOnMapRenderCallbadk(callback1);

        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //配置定位图层显示方式
        MyLocationConfiguration mLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null, 0xAAFFFF88, 0xAA00FF00);
        mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);
        mBaiduMap.getUiSettings().setCompassEnabled(true);
        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定位到我的位置
        findMe();

        is_set = true;
    }

    public void onResume() {
        if (is_set) {
            mMapView.onResume();
        }
    }

    public void onPause() {
        if (is_set) {
            mMapView.onPause();
        }
    }

    public void onDestroy() {
        if (is_set) {
            myOrientationListener.onStop();
            mLocationClient.stop();
            mBaiduMap.setMyLocationEnabled(false);
            mMapView.onDestroy();
            mMapView = null;
        }
    }

    public void findMe() {
        new Thread(() -> {
            //等待地图加载渲染完成
            while (!(flag_MapLoaded && flag_MapRender)) {
                synchronized (Thread.currentThread()) {
                    try {
                        Thread.currentThread().wait(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

//                    flag_MapRender = true;//---------------------------------------------------------------------------------------------------------------------
//                    // 使用 Handler 在 UI 线程中显示 Toast-------------------------------------------------------------------------------------------------------------------------------
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(() -> Toast.makeText(context.getApplicationContext(), flag_MapLoaded + Boolean.toString(flag_MapRender), Toast.LENGTH_SHORT).show());
                }
            }
            //定位显示
            if (but_findme == 0) {
                //配置定位图层显示方式
                MyLocationConfiguration mLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, null, 0xAAFFFF88, 0xAA00FF00);
                mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);
                // 切换到3D视角
                MapStatus mapStatus = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(-45).build();
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                mBaiduMap.setMapStatus(mapStatusUpdate);
                //地图点(我的位置)居中
                LatLng ll = new LatLng(mCurrentLat, mCurrentLon);
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll, 18);
                mBaiduMap.animateMapStatus(update);
            } else {
                //配置定位图层显示方式
                MyLocationConfiguration mLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null, 0xAAFFFF88, 0xAA00FF00);
                mBaiduMap.setMyLocationConfiguration(mLocationConfiguration);
                // 切换到2D视角
                MapStatus mapStatus = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(0).build();
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                mBaiduMap.setMapStatus(mapStatusUpdate);
                // 摆正地图
                MapStatus rotateMapStatus = new MapStatus.Builder(mBaiduMap.getMapStatus()).rotate(0).build();
                MapStatusUpdate rotateMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(rotateMapStatus);
                mBaiduMap.setMapStatus(rotateMapStatusUpdate);
                //地图点(我的位置)居中
                LatLng ll = new LatLng(mCurrentLat, mCurrentLon);
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll, 17);
                mBaiduMap.animateMapStatus(update);
            }
        }).start();
    }

    public void changeMapType() {
        if (but_maptype == 1) {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        } else {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        }
    }

}
