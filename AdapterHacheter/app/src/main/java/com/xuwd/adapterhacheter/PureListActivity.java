package com.xuwd.adapterhacheter;
//注意：这个actiity没有lauyout xml文件

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class PureListActivity extends ListActivity implements AdapterView.OnItemClickListener{
    String[] strs = {"1","2","3","4","5"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //列表项的数据

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,strs);
        setListAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long itemId) {
        int ord=position+1;
//        view.findViewById(itemId);
        String showText = "点击第" + ord + "项，文本内容为：" + strs[position] + "，ID为：" + itemId;
        Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
    }
}
