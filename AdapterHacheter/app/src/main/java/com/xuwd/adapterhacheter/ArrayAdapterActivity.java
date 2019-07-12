package com.xuwd.adapterhacheter;
//通过平台添加activity和拷贝的区别：activity在manifests文件中的注册
//1、实现ListView与ArrayAdapter联合的基本型

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ArrayAdapterActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView listView=null;
    private String items[]={"111","2222","33333","4444"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_array_adapter);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_gallery_item,items);
        listView=findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long itemId) {
        int ord=position+1;
//        view.findViewById(itemId);
        String showText = "点击第" + ord + "项，文本内容为：" + items[position] + "，ID为：" + itemId;
        Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
    }
}
