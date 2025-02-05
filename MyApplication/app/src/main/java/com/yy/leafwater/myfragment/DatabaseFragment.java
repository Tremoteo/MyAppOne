package com.yy.leafwater.myfragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.yy.leafwater.R;
import com.yy.leafwater.mydb.MyDBEngine;
import com.yy.leafwater.mydb.MyTable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DatabaseFragment extends Fragment implements View.OnClickListener {
    private View databaseView;

    private MyDBEngine myDBEngine;
    private ActivityResultLauncher<Intent> exportLauncher;

    public DatabaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (databaseView == null) {
            databaseView = inflater.inflate(R.layout.fragment_database, container, false);

            myDBEngine = MyDBEngine.getINSTANCE(requireActivity().getApplicationContext());
            exportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result != null) {
                            Intent intent = result.getData();
                            if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
                                Uri uri = intent.getData();
                                try {
                                    List<MyTable> list = myDBEngine.getAll();
                                    OutputStream outputStream = requireActivity().getContentResolver().openOutputStream(uri, "wa");
                                    if (outputStream != null) {
                                        // 将数据写入文件
                                        String str = "ID,容值(pF),温度,纬度,经度,精度,时间,时间,设备状态\n";
                                        outputStream.write(str.getBytes());
                                        for (MyTable temp : list) {
                                            str = temp.getId() + "," + temp.getC() + "," + temp.getTemperature() + "," +
                                                    temp.getLat() + "," + temp.getLon() + "," + temp.getAccuracy() + "," + temp.getTime() + "," + temp.getState() + "\n";
                                            outputStream.write(str.getBytes());
                                        }
                                        outputStream.close();
                                        Toast.makeText(requireActivity(), "保存成功:" + uri.getPath(), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(requireActivity(), "保存失败", Toast.LENGTH_SHORT).show();
                                } catch (ExecutionException | InterruptedException e) {
                                    Toast.makeText(requireActivity(), "保存失败", Toast.LENGTH_SHORT).show();
                                    throw new RuntimeException(e);
                                }
                            }

                        }
                    });

            initViews();

        } else {
            ViewGroup parent = (ViewGroup) databaseView.getParent();
            if (null != parent) {
                parent.removeView(databaseView);
            }
        }

        return databaseView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_export) {
            try {
                exportDataToTxt();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else if (v.getId() == R.id.btn_clean) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("确认操作");
            builder.setMessage("您确定清空数据库吗？");
            builder.setPositiveButton("确定", (dialog, which) -> {
                myDBEngine.deleteAll();
                Toast.makeText(requireActivity(), "数据库已清空", Toast.LENGTH_LONG).show();
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        }
    }

    private void initViews() {
        Button btn_export = databaseView.findViewById(R.id.btn_export);
        Button btn_clean = databaseView.findViewById(R.id.btn_clean);

        btn_export.setOnClickListener(this);
        btn_clean.setOnClickListener(this);
    }

    private void exportDataToTxt() throws ExecutionException, InterruptedException {
        // 创建保存文件的Intent
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        // 设置文件类型为txt
        intent.setType("text/plain");
        // 设置文件名
        intent.putExtra(Intent.EXTRA_TITLE, "data.txt");
        // 设置允许用户选择文件的位置和文件名
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // 启动保存文件的Activity
        exportLauncher.launch(intent);
    }
}