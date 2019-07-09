package com.xuwd.adapterhacheter;
//通过平台添加activity和拷贝的区别：activity在manifests文件中的注册
//1、实现ListView与ArrayAdapter联合的基本型

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ArrayAdapterActivity extends AppCompatActivity {
    private ListView listView=null;
    private String items[]={"111","2222","33333","4444"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_array_adapter);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,items);
        listView=findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
    }
}
