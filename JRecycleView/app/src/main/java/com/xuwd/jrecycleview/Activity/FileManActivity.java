package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;
import com.xuwd.jrecycleview.Utility.*;

public class FileManActivity extends AppCompatActivity {
    private RecycleAdapter mFileListAdapter;
    RecyclerView mFileListView;

    private String mDir;
    private String outSdcard;
    private String innerSdcard;
    private static final String SDCARD_ROOT_DEFAULT = "/storage";
    private static int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_man);

        initDirNavigator();
        initFileList();

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

    private void initFileList(){
        mFileListView =findViewById(R.id.fileListView);

        RecyclerView.LayoutManager verticalLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        mFileListView.setLayoutManager(verticalLayoutManager);

        RecycleAdapter mFileListAdapter=new RecycleAdapter(R.layout.list_filelist,initData(0));
        mFileListView.setAdapter(mFileListAdapter);
    }

    public ArrayList<StorageUtil.FileItem> initData(int iStart){
        ArrayList<StorageUtil.FileItem> items=new ArrayList<StorageUtil.FileItem>();

        outSdcard = StorageUtil.getStoragePath(this, true);
        innerSdcard = StorageUtil.getStoragePath(this,true);

        for(int i=iStart;i<20;i++){
            StorageUtil.FileItem fileItem= new StorageUtil.FileItem("item"+i,"t",false);
            items.add(fileItem);
        }
        return items;
    }

    public ArrayList<String> curerentDirItems(){
        ArrayList<String> items=new ArrayList<String>();

        return items;
    }
    public void reList(int position){
        mFileListAdapter =new RecycleAdapter(R.layout.list_filelist,initData(position));
        mFileListView.setAdapter(mFileListAdapter);
    }

}
