package com.xuwd.adapterhacheter;
//注意：这个actiity没有lauyout xml文件
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class PureListActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //列表项的数据
        String[] strs = {"1","2","3","4","5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,strs);
        setListAdapter(adapter);
    }
}
