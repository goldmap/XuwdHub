package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;

import java.util.ArrayList;

public class FileManActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_man);

        RecyclerView dirRcyclerView=findViewById(R.id.dirRecycleView);

        RecyclerView.LayoutManager horizontalLayoutManager=new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        dirRcyclerView.setLayoutManager(horizontalLayoutManager);

        RecycleAdapter adapter=new RecycleAdapter(R.layout.list_dir_navigator,initData());
//        RecycleAdapter adapter=new RecycleAdapter(R.layout.list_simple_text,initData());
        dirRcyclerView.setAdapter(adapter);
    }

    public ArrayList<String> initData(){
        ArrayList<String> data=new ArrayList<String>();
        for(int i=0;i<20;i++){
            data.add("item"+i);
        }
        return data;
    }
}
