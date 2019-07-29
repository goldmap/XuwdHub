package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;
import com.xuwd.jrecycleview.Utility.*;

public class FileManActivity extends AppCompatActivity {
    private ArrayAdapter<String> mListAdapter;
    private ListView mListView;

    private List<StorageUtil.FileItem> mFileItemList;
    private String mDir;
    private String outSdcard;
    private String innerSdcard;
    private static final String SDCARD_ROOT_DEFAULT = "/storage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_man);
        initDirNavigator();
        initDirList();
    }
    private void initDirNavigator(){
        RecyclerView dirRecyclerView=findViewById(R.id.dirRecycleView);

        RecyclerView.LayoutManager horizontalLayoutManager=new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        dirRecyclerView.setLayoutManager(horizontalLayoutManager);

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
        dirRecyclerView.setAdapter(mDirNavigatorAdapter);
    }

    private void initDirList(){
        mListView =findViewById(R.id.fileListView);
        mListAdapter =new ArrayAdapter<String>(this,R.layout.list_pure_text,R.id.listItemText,initData(0));

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
        dirRecyclerView.setAdapter(mDirNavigatorAdapter);

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
