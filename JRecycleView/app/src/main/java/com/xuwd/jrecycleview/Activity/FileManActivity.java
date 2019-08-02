package com.xuwd.jrecycleview.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xuwd.jrecycleview.Adapter.RecycleAdapter;
import com.xuwd.jrecycleview.R;
import com.xuwd.jrecycleview.Utility.*;

public class FileManActivity extends AppCompatActivity {
    private RecycleAdapter mFileListAdapter;
    RecyclerView mFileListView;
    RecyclerView mDirNavigatorView;
    ArrayList<StorageUtil.FileItem> dirList=new ArrayList<>();

    private String mDir;
    private String outSdcard;
    private String innerSdcard;
    private static final String SDCARD_ROOT_DEFAULT = "/storage";
    private static int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_man);

        dirList.add(new StorageUtil.FileItem("root","root",true));

        mDirNavigatorView=findViewById(R.id.dirRecycleView);
        RecyclerView.LayoutManager horizontalLayoutManager=new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        mDirNavigatorView.setLayoutManager(horizontalLayoutManager);
        setDirNavigatoraAdapter("root");

        mFileListView =findViewById(R.id.fileListView);
        RecyclerView.LayoutManager verticalLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        mFileListView.setLayoutManager(verticalLayoutManager);
        setFileListAdpter("root");
    }

    private void setDirNavigatoraAdapter(String dirPath){
        RecycleAdapter mDirNavigatorAdapter=new RecycleAdapter(R.layout.list_dir_navigator, dirList);
       mDirNavigatorAdapter.setIcon(R.mipmap.forword,R.mipmap.forword);

        mDirNavigatorAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                StorageUtil.FileItem dirItem=dirList.get(position);
                Toast.makeText(getBaseContext(),fileItem.fileName+"|"+fileItem.filePath,Toast.LENGTH_SHORT).show();
                setDirNavigatoraAdapter(fileItem.filePath);
                setFileListAdpter();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mDirNavigatorView.setAdapter(mDirNavigatorAdapter);
    }

    private void setFileListAdpter(final String dirPath){
        final RecycleAdapter mFileListAdapter=new RecycleAdapter(R.layout.list_filelist, getDirList(dirPath));
        mFileListAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                StorageUtil.FileItem fileItem=mFileListAdapter.mData.get(position);
                Toast.makeText(getBaseContext(),fileItem.fileName+"|"+fileItem.filePath,Toast.LENGTH_SHORT).show();
                setFileListAdpter(fileItem.filePath);
                setDirNavigatoraAdapter(dirPath);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mFileListView.setAdapter(mFileListAdapter);
    }

    public ArrayList<StorageUtil.FileItem> getDirList(String dirPath){
        ArrayList<StorageUtil.FileItem> fileItemList=new ArrayList<>();
        StorageUtil.FileItem fileItem=null;

        if(dirPath=="root"){
            outSdcard = JUtil.getStoragePath(this, true);
            innerSdcard = JUtil.getStoragePath(this,false);

            if(outSdcard!=null){
                fileItem=new StorageUtil.FileItem("SD card",outSdcard,true);
                fileItemList.add(fileItem);
            }
            if(innerSdcard!=null){
                fileItem=new StorageUtil.FileItem("Internal storage",innerSdcard,true);
                fileItemList.add(fileItem);
            }
        }else{
            File dirNow=new File(dirPath);
            File[] files=dirNow.listFiles();
            if(files != null){
                for(int i=0;i<files.length;i++){
                    if(files[i].isDirectory()){
                        fileItemList.add(new StorageUtil.FileItem(files[i].getName(),files[i].getPath(),true));
                    }else{
                        fileItemList.add(new StorageUtil.FileItem(files[i].getName(),files[i].getPath(),false));
                    }
                }
            }
        }
        return fileItemList;
    }

    public ArrayList<String> curerentDirItems(){
        ArrayList<String> items=new ArrayList<String>();
        return items;
    }
    public void reList(String dirPath){
        mFileListAdapter =new RecycleAdapter(R.layout.list_filelist, getDirList(dirPath));
        mFileListView.setAdapter(mFileListAdapter);
    }

}
