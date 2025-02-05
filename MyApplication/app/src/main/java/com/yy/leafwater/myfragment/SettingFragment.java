package com.yy.leafwater.myfragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.yy.leafwater.MyListInfo;
import com.yy.leafwater.R;


public class SettingFragment extends Fragment implements View.OnClickListener {
    private View settingView;

    private Spinner cdcDischargeResistance;
    private ArrayAdapter<CharSequence> adapter1;
    private EditText CMEAS_CYTIME;
    private Spinner cdcFalseMeasurement;
    private ArrayAdapter<CharSequence> adapter2;
    private EditText C_AVRG;
    private Spinner rdcFalseMeasurement;
    private ArrayAdapter<CharSequence> adapter3;
    private Spinner rdcAverageNumber;
    private ArrayAdapter<CharSequence> adapter4;

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (settingView == null) {
            settingView = inflater.inflate(R.layout.fragment_setting, container, false);

            //初始化控件
            initViews();
        } else {
            ViewGroup parent = (ViewGroup) settingView.getParent();
            if (null != parent) {
                parent.removeView(settingView);
            }
        }

        return settingView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        cdcDischargeResistance = settingView.findViewById(R.id.cdcDischargeResistance);
        CMEAS_CYTIME = settingView.findViewById(R.id.CMEAS_CYTIME);
        cdcFalseMeasurement = settingView.findViewById(R.id.cdcFalseMeasurement);
        C_AVRG = settingView.findViewById(R.id.C_AVRG);
        rdcFalseMeasurement = settingView.findViewById(R.id.rdcFalseMeasurement);
        rdcAverageNumber = settingView.findViewById(R.id.rdcAverageNumber);
        Button btn_application = settingView.findViewById(R.id.btn_application);
        Button btn_reset = settingView.findViewById(R.id.btn_reset);

        //设置按钮监听
        btn_application.setOnClickListener(this);
        btn_reset.setOnClickListener(this);

        //创建数组适配器，并将其设置为Spinner的适配器
        adapter1 = ArrayAdapter.createFromResource(requireActivity().getApplicationContext(),
                R.array.options_cdcDischargeResistance, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cdcDischargeResistance.setAdapter(adapter1);

        adapter2 = ArrayAdapter.createFromResource(requireActivity().getApplicationContext(),
                R.array.options_cdcFalseMeasurement, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cdcFalseMeasurement.setAdapter(adapter2);

        adapter3 = ArrayAdapter.createFromResource(requireActivity().getApplicationContext(),
                R.array.options_rdcFalseMeasurement, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rdcFalseMeasurement.setAdapter(adapter3);

        adapter4 = ArrayAdapter.createFromResource(requireActivity().getApplicationContext(),
                R.array.options_rdcAverageNumber, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rdcAverageNumber.setAdapter(adapter4);

        //点击空白退出编辑
        settingView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 清除EditText的焦点
                CMEAS_CYTIME.clearFocus();
                C_AVRG.clearFocus();

                // 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(CMEAS_CYTIME.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(C_AVRG.getWindowToken(), 0);
            }
            return false;
        });

        //显示当前状态
        currentStatus();
    }

    @Override
    public void onClick(View v) {
        //获取当前寄存器设置
        long[] array = getREG();

        if (v.getId() == R.id.btn_application) {
            //设置CDC放电电阻
            String srt = cdcDischargeResistance.getSelectedItem().toString();
            switch (srt) {
                case "180kOhm":
                    array[2] = (array[2] & 0xFFF8FFL) | (0b100L << 8);
                    break;
                case "90kOhm":
                    array[2] = (array[2] & 0xFFF8FFL) | (0b101L << 8);
                    break;
                case "30kOhm":
                    array[2] = (array[2] & 0xFFF8FFL) | (0b110L << 8);
                    break;
                case "10kOhm":
                    array[2] = (array[2] & 0xFFF8FFL) | (0b111L << 8);
                    break;
            }
            //设置CDC假测次数
            srt = cdcFalseMeasurement.getSelectedItem().toString();
            switch (srt) {
                case "0":
                    array[3] = array[3] & 0xFF9FFFL;
                    break;
                case "1":
                    array[3] = (array[3] & 0xFF9FFFL) | (0b01L << 13);
                    break;
                case "2":
                    array[3] = (array[3] & 0xFF9FFFL) | (0b10L << 13);
                    break;
                case "4":
                    array[3] = (array[3] & 0xFF9FFFL) | (0b11L << 13);
                    break;
            }
            //设置RDC假测次数
            srt = rdcFalseMeasurement.getSelectedItem().toString();
            if (srt.equals("2")) {
                array[6] = array[6] & 0xFF7FFFL;
            } else if (srt.equals("8")) {
                array[6] = (array[6] & 0xFF7FFFL) | (0b1L << 15);
            }
            //设置RDC平均次数
            srt = rdcAverageNumber.getSelectedItem().toString();
            switch (srt) {
                case "1":
                    array[5] = array[5] & 0x3FFFFFL;
                    break;
                case "4":
                    array[5] = (array[5] & 0x3FFFFFL) | (0b01L << 22);
                    break;
                case "8":
                    array[5] = (array[5] & 0x3FFFFFL) | (0b10L << 22);
                    break;
                case "16":
                    array[5] = (array[5] & 0x3FFFFFL) | (0b11L << 22);
                    break;
            }
            //设置CDC周期时间
            srt = CMEAS_CYTIME.getText().toString();
            if (srt.matches("-?\\d+(\\.\\d+)?") && Long.parseLong(srt) >= 1 && Long.parseLong(srt) <= 1024) {
                array[4] = (array[4] & 0xFC00FFL) | ((Long.parseLong(srt) - 1) << 8);
            }
            //设置CDC平均次数
            srt = C_AVRG.getText().toString();
            if (srt.matches("-?\\d+(\\.\\d+)?") && Long.parseLong(srt) >= 1 && Long.parseLong(srt) <= 8191) {
                array[3] = (array[3] & 0xFFE000L) | Long.parseLong(srt);
            }

            //保存配置数据到SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            StringBuilder stringBuilder = new StringBuilder();
            for (long value : array) {
                stringBuilder.append(value).append(",");
            }
            String arrayString = stringBuilder.toString();
            editor.putString("REG", arrayString);
            editor.apply();
        } else if (v.getId() == R.id.btn_reset) {
            //保存初始配置数据到SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            StringBuilder stringBuilder = new StringBuilder();
            for (long value : MyListInfo.REG) {
                stringBuilder.append(value).append(",");
            }
            String arrayString = stringBuilder.toString();
            editor.putString("REG", arrayString);
            editor.apply();
        }
        //显示当前状态
        currentStatus();
    }

    private void currentStatus() {
        //获取当前寄存器设置
        long[] array = getREG();
        //显示当前CDC放电电阻
        if ((array[2] & 0b111L << 8) == 0b100L << 8) {
            cdcDischargeResistance.setSelection(adapter1.getPosition("180kOhm"));
        } else if ((array[2] & 0b111L << 8) == 0b101L << 8) {
            cdcDischargeResistance.setSelection(adapter1.getPosition("90kOhm"));
        } else if ((array[2] & 0b111L << 8) == 0b110L << 8) {
            cdcDischargeResistance.setSelection(adapter1.getPosition("30kOhm"));
        } else if ((array[2] & 0b111L << 8) == 0b111L << 8) {
            cdcDischargeResistance.setSelection(adapter1.getPosition("10kOhm"));
        }
        //显示当前CDC假测次数
        if ((array[3] & 0b11L << 13) == 0L) {
            cdcFalseMeasurement.setSelection(adapter2.getPosition("0"));
        } else if ((array[3] & 0b11L << 13) == 0b01L << 13) {
            cdcFalseMeasurement.setSelection(adapter2.getPosition("1"));
        } else if ((array[3] & 0b11L << 13) == 0b10L << 13) {
            cdcFalseMeasurement.setSelection(adapter2.getPosition("2"));
        } else if ((array[3] & 0b11L << 13) == 0b11L << 13) {
            cdcFalseMeasurement.setSelection(adapter2.getPosition("4"));
        }
        //显示当前RDC假测次数
        if ((array[6] & 0b1L << 15) == 0L) {
            rdcFalseMeasurement.setSelection(adapter3.getPosition("2"));
        } else if ((array[6] & 0b1L << 15) == 0b1L << 15) {
            rdcFalseMeasurement.setSelection(adapter3.getPosition("8"));
        }
        //显示当前RDC平均次数
        if ((array[5] & 0b11L << 22) == 0L) {
            rdcAverageNumber.setSelection(adapter4.getPosition("1"));
        } else if ((array[5] & 0b11L << 22) == 0b01L << 22) {
            rdcAverageNumber.setSelection(adapter4.getPosition("4"));
        } else if ((array[5] & 0b11L << 22) == 0b10L << 22) {
            rdcAverageNumber.setSelection(adapter4.getPosition("8"));
        } else if ((array[5] & 0b11L << 22) == 0b11L << 22) {
            rdcAverageNumber.setSelection(adapter4.getPosition("16"));
        }
        //显示当前CDC周期时间
        String srt = Long.toString(((array[4] & 0x3FF00L) >> 8) + 1);
        CMEAS_CYTIME.setText(srt);
        //显示当前CDC平均次数
        String srt1 = Long.toString(array[3] & 0x1FFFL);
        C_AVRG.setText(srt1);
    }

    private long[] getREG() {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        // 使用getString()方法获取保存的字符串
        String arrayString = sharedPreferences.getString("REG", "");
        // 将字符串转换为long型数组
        String[] arrayStringArray = arrayString.split(",");
        long[] array = new long[arrayStringArray.length];
        for (int i = 0; i < arrayStringArray.length; i++) {
            array[i] = Long.parseLong(arrayStringArray[i]);
        }
        return array;
    }

}