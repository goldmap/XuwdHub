package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;

import java.util.ArrayList;

public class FileManActivity extends AppCompatActivity {
    private ArrayAdapter<String> mListAdapter;
    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_man);

        RecyclerView dirRcyclerView=findViewById(R.id.dirRecycleView);

        RecyclerView.LayoutManager horizontalLayoutManager=new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        dirRcyclerView.setLayoutManager(horizontalLayoutManager);

        RecycleAdapter mDirNavigatorAdapter=new RecycleAdapter(R.layout.list_dir_navigator,initData(0));
        mDirNavigatorAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
               reList(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
//        RecycleAdapter mDirNavigatorAdapter=new RecycleAdapter(R.layout.list_simple_text,initData());
        dirRcyclerView.setAdapter(mDirNavigatorAdapter);

        mListView =findViewById(R.id.fileListView);
        //mListAdapter =new ArrayAdapter<String>(this,R.layout.list_pure_text,R.id.listItemText,initData(0));
        //mListView.setAdapter(mListAdapter);
    }

    public ArrayList<String> initData(int iStart){
        ArrayList<String> data=new ArrayList<String>();
        for(int i=iStart;i<20;i++){
            data.add("item"+i);
        }
        return data;
    }

    public void reList(int position){
        mListAdapter =new ArrayAdapter<String>(this,R.layout.list_pure_text,R.id.listItemText,initData(position));
        mListView.setAdapter(mListAdapter);
    }
}
