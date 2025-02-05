package com.yy.leafwater;

import static com.yy.leafwater.MyOperate.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yy.leafwater.mydb.MyDBEngine;
import com.yy.leafwater.mydb.MyTable;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {
    private static MyDBEngine myDBEngine;
    private final View fragmentView;
    private boolean mIsSelectMode = false;// 记录多选模式状态
    private final SparseBooleanArray mSelectedPositions = new SparseBooleanArray();// 记录每个item的选中状态

    public MyRecyclerViewAdapter(Context context, View view) {
        fragmentView = view;
        myDBEngine = MyDBEngine.getINSTANCE(context.getApplicationContext());
    }

    @NonNull
    @Override
    public MyRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.recyclerview_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewAdapter.MyViewHolder holder, int position) {
        //设置可见性
        holder.mCheckBox.setVisibility(mIsSelectMode ? View.VISIBLE : View.GONE);
        //设置状态
        holder.mTextView.setText(data.get(position).getName());
        holder.mCheckBox.setChecked(mSelectedPositions.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        private final CheckBox mCheckBox;

        @SuppressLint("NotifyDataSetChanged")
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.item);
            mCheckBox = itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(v -> {
                if (mIsSelectMode) {
                    mSelectedPositions.put(getBindingAdapterPosition(), !mSelectedPositions.get(getBindingAdapterPosition()));
                    setSelectNum();
                    notifyDataSetChanged();
                }
            });
            mCheckBox.setOnClickListener(v -> {
                if (mIsSelectMode) {
                    mSelectedPositions.put(getBindingAdapterPosition(), !mSelectedPositions.get(getBindingAdapterPosition()));
                    setSelectNum();
                    notifyDataSetChanged();
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (mIsSelectMode) {
                    mSelectedPositions.put(getBindingAdapterPosition(), !mSelectedPositions.get(getBindingAdapterPosition()));
                    setSelectNum();
                    notifyDataSetChanged();
                    return true;
                }
                setSelectMode(true);
                return true;
            });
        }
    }

    @NonNull
    private List<Integer> getSelectedPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            int position = mSelectedPositions.keyAt(i);
            if (mSelectedPositions.get(position)) {
                positions.add(position);
            }
        }
        return positions;
    }

    private void setSelectNum() {
        int temp = 0;
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            int position = mSelectedPositions.keyAt(i);
            if (mSelectedPositions.get(position)) {
                temp++;
            }
        }

        TextView textView = fragmentView.findViewById(R.id.select_num);
        String str = "  " + temp;
        textView.setText(str);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll() {
        for (int i = 0; i < data.size(); i++) {
            mSelectedPositions.put(i, true);
        }
        setSelectNum();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void unselectAll() {
        mSelectedPositions.clear();
        setSelectNum();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteSelected() {
        List<Integer> positions = getSelectedPositions();
        for (int i = positions.size() - 1; i >= 0; i--) {
            int position = positions.get(i);
            //删除数据库对应条目
            MyTable myTable = new MyTable();
            myTable.setId(data.get(position).getId());
            myDBEngine.deleteMyTables(myTable);
            //删除RecyclerView数据
            data.remove(position);
        }
        mSelectedPositions.clear();
        setSelectMode(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectMode(boolean isSelectMode) {
        mIsSelectMode = isSelectMode;
        fragmentView.findViewById(R.id.constraintlayout2).setVisibility(mIsSelectMode ? View.VISIBLE : View.GONE);
        fragmentView.findViewById(R.id.linearlayout2).setVisibility(mIsSelectMode ? View.GONE : View.VISIBLE);
        notifyDataSetChanged();
    }

}
