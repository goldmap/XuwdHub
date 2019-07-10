package com.xuwd.jlistview_click;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private static String[] strs = new String[]{ "first", "second", "third", "fourth", "fifth"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_gallery_item,strs);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //大多数情况下，position和id相同，并且都从0开始
        int ord=position+1;
        String showText = "点击第" + ord + "项，文本内容为：" + strs[position] + "，ID为：" + id;
        Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
    }
}

/*
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ListView myListView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this, getData(), R.layout.item, new String[]{"img", "text"}, new int[]{R.id.img, R.id.txt});
        myListView = (ListView) findViewById(R.id.listView);
        //设置监听器
        myListView.setAdapter(simpleAdapter);
        myListView.setOnItemClickListener(this);
    }

    private List<Map<String, Object>> getData() {
        for (int i = 0; i < 6; i++) {
            Map<String, Object>map = new HashMap<String, Object>();
            map.put("img", R.mipmap.ic_launcher);
            map.put("text", "初始simpleAdapter"+(i+1));
            data.add(map);
        }

        return data;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //通过view获取其内部的组件，进而进行操作
        String text = (String) ((TextView)view.findViewById(R.id.txt)).getText();
        //大多数情况下，position和id相同，并且都从0开始
        String showText = "点击第" + position + "项，文本内容为：" + text + "，ID为：" + id;
        Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
    }
}
*/