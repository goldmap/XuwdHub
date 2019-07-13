package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;

import java.util.ArrayList;

public class RecycleStaggerGridActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_linear);

        RecyclerView recyclerView=findViewById(R.id.linearRecycleView);
        RecyclerView.LayoutManager layoutManager=new StaggeredGridLayoutManager( 3, RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        RecycleAdapter adapter=new RecycleAdapter(initData());
        recyclerView.setAdapter(adapter);

//        recyclerLinear.setItemAnimator(new DefaultItemAnimator());
//        recyclerLinear.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    public ArrayList<String> initData(){
        ArrayList<String> data=new ArrayList<String>();
        for(int i=0;i<20;i++){
            data.add("item"+i);
        }
        return data;
    }
}