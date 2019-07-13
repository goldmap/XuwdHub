package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;

import java.util.ArrayList;

public class RecycleLinearActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_linear);

        RecyclerView recyclerLinear=findViewById(R.id.linearRecycleView);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recyclerLinear.setLayoutManager(layoutManager);

        RecycleAdapter adapter=new RecycleAdapter(initData());
        recyclerLinear.setAdapter(adapter);

//        recyclerLinear.setItemAnimator(new DefaultItemAnimator());
//        recyclerLinear.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(RecycleLinearActivity.this,"Here:"+position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

    }

    public ArrayList<String> initData(){
        ArrayList<String> data=new ArrayList<String>();
        for(int i=0;i<20;i++){
            data.add("item"+i);
        }
        return data;
    }


}
