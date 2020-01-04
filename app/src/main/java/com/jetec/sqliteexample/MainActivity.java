package com.jetec.sqliteexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    String TAG = MainActivity.class.getSimpleName() + "My";

    private final String DB_NAME = "MyList.db";
    private String TABLE_NAME = "MyTable";
    private final int DB_VERSION = 1;
    SQLiteDataBaseHelper mDBHelper;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();//取得所有資料
    ArrayList<HashMap<String, String>> getNowArray = new ArrayList<>();//取得被選中的項目資料

    EditText edName, edPhone, edHobby, edElse;
    Button btCreate, btModify, btClear;
    MyAdapter myAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);
        mDBHelper = new SQLiteDataBaseHelper(this, DB_NAME
                , null, DB_VERSION, TABLE_NAME);//初始化資料庫
        mDBHelper.chickTable();//確認是否存在資料表，沒有則新增
        arrayList = mDBHelper.showAll();//撈取資料表內所有資料
        itemSetting();//連接所有元件
        recyclerViewSetting();//設置RecyclerView
        buttonFunction();//設置按鈕功能


    }//onCreate End

    private void buttonFunction() {//設置按鈕功能
        btClear.setOnClickListener(v -> {
            clearAll();//清空目前所選以及所有editText
        });
        btCreate.setOnClickListener(v -> {
            mDBHelper.addData(edName.getText().toString()
                    ,edPhone.getText().toString()
                    ,edHobby.getText().toString()
                    ,edElse.getText().toString());
            arrayList = mDBHelper.showAll();
            myAdapter.notifyDataSetChanged();
            clearAll();
        });
        btModify.setOnClickListener(v -> {
            mDBHelper.modify(getNowArray.get(0).get("id")
                    ,edName.getText().toString()
                    ,edPhone.getText().toString()
                    ,edHobby.getText().toString()
                    ,edElse.getText().toString());
            arrayList = mDBHelper.showAll();
            myAdapter.notifyDataSetChanged();
            clearAll();//清空目前所選以及所有editText

        });
    }

    private void clearAll() {//清空目前所選以及所有editText
        edName.setText("");
        edElse.setText("");
        edHobby.setText("");
        edPhone.setText("");
        getNowArray.clear();
    }

    private void recyclerViewSetting() {//設置RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        setRecyclerFunction(recyclerView);//設置RecyclerView手勢功能
    }

    private void itemSetting() {//連接所有元件
        btCreate = findViewById(R.id.button_Create);
        btModify = findViewById(R.id.button_Modify);
        btClear = findViewById(R.id.button_Clear);
        edName = findViewById(R.id.editText_Name);
        edPhone = findViewById(R.id.editText_Phone);
        edHobby = findViewById(R.id.editText_Hobby);
        edElse = findViewById(R.id.editText_else);
    }


    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {//設置Adapter
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvTitle.setText(arrayList.get(position).get("name"));

            holder.itemView.setOnClickListener((v) -> {
                getNowArray.clear();
                getNowArray = mDBHelper.searchById(arrayList.get(position).get("id"));
                try {
                    edName.setText(getNowArray.get(0).get("name"));
                    edPhone.setText(getNowArray.get(0).get("phone"));
                    edHobby.setText(getNowArray.get(0).get("hobby"));
                    edElse.setText(getNowArray.get(0).get("elseInfo"));
                } catch (Exception e) {
                    Log.d(TAG, "onBindViewHolder: " + e.getMessage());
                }

            });

        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    private void setRecyclerFunction(RecyclerView recyclerView){
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {//設置RecyclerView手勢功能
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch (direction){
                    case ItemTouchHelper.LEFT:
                    case ItemTouchHelper.RIGHT:
                        mDBHelper.deleteByIdEZ(arrayList.get(position).get("id"));
                        arrayList.remove(position);
                        arrayList = mDBHelper.showAll();
                        myAdapter.notifyItemRemoved(position);

                        break;

                }
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

}
