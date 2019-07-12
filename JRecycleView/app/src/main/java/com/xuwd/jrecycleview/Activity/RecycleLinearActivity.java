package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;

import java.util.ArrayList;

public class RecycleLinearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_linear);

        RecyclerView recyclerLinear=findViewById(R.id.recycleLinear);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recyclerLinear.setLayoutManager(layoutManager);

        RecycleAdapter adapter=new RecycleAdapter(initData());
        recyclerLinear.setAdapter(adapter);
        recyclerLinear.setItemAnimator(new DefaultItemAnimator());
        // 设置Item之间间隔样式
        recyclerLinear.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    public ArrayList<String> initData(){
        ArrayList<String> data=new ArrayList<String>();
        for(int i=0;i<20;i++){
            data.add("item"+i);
        }
        return data;
    }
}
