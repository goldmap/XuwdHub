package com.xuwd.jvideoplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class FileManActivity extends AppCompatActivity {
    private RecycleAdapter mFileListAdapter;
    RecyclerView mFileListView;
    RecyclerView mDirNavigatorView;
    ArrayList<JUtil.FileItem> dirList=new ArrayList<>();

    private String outSdcard;
    private String innerSdcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_man);

        dirList.add(new JUtil.FileItem("root","root",true));

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
                JUtil.FileItem dirItem=dirList.get(position);
                while(dirList!=null) {
                    if (dirList.get(dirList.size() - 1).fileName != dirItem.fileName) {
                        dirList.remove(dirList.get(dirList.size() - 1));
                    }else{
                        break;
                    }
                }
                Toast.makeText(getBaseContext(),dirItem.fileName+"|"+dirItem.filePath,Toast.LENGTH_SHORT).show();
                setDirNavigatoraAdapter(dirItem.filePath);
                setFileListAdpter(dirItem.filePath);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mDirNavigatorView.setAdapter(mDirNavigatorAdapter);
        mDirNavigatorView.smoothScrollToPosition(dirList.size()-1);
    }

    private void setFileListAdpter(final String dirPath){
        final RecycleAdapter mFileListAdapter=new RecycleAdapter(R.layout.list_filelist, getDirList(dirPath));
        mFileListAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                JUtil.FileItem fileItem=mFileListAdapter.mData.get(position);
                if(!fileItem.isZip){
                    Intent intent=new Intent(getBaseContext(),VideoPlayActivity.class);
                    intent.putExtra("filePath",fileItem.filePath);
                    startActivity(intent);
                    return;
                }else{
                    Toast.makeText(getBaseContext(),fileItem.fileName+"|"+fileItem.filePath,Toast.LENGTH_SHORT).show();
                    setFileListAdpter(fileItem.filePath);

                    dirList.add(fileItem);
                    setDirNavigatoraAdapter(dirPath);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mFileListView.setAdapter(mFileListAdapter);
    }

    public ArrayList<JUtil.FileItem> getDirList(String dirPath){
        ArrayList<JUtil.FileItem> fileItemList=new ArrayList<>();
        JUtil.FileItem fileItem=null;

        if(dirPath=="root"){
            outSdcard = JUtil.getStoragePath(this, true);
            innerSdcard = JUtil.getStoragePath(this,false);

            if(outSdcard!=null){
                fileItem=new JUtil.FileItem("SD card",outSdcard,true);
                fileItemList.add(fileItem);
            }
            if(innerSdcard!=null){
                fileItem=new JUtil.FileItem("Internal storage",innerSdcard,true);
                fileItemList.add(fileItem);
            }
        }else{
            File dirNow=new File(dirPath);
            File[] files=dirNow.listFiles();
            if(files != null){
                for(int i=0;i<files.length;i++){
                    if(files[i].isDirectory()){
                        fileItemList.add(new JUtil.FileItem(files[i].getName(),files[i].getPath(),true));
                    }
                }
                for(int i=0;i<files.length;i++){
                    if(!files[i].isDirectory()){
                        fileItemList.add(new JUtil.FileItem(files[i].getName(),files[i].getPath(),false));
                    }
                }
            }
        }
        return fileItemList;
    }

}
