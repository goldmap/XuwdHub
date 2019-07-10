package com.xuwd.adapterhacheter;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalListActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.item_normallist, new String[] { "title",  "img" }, new int[] { R.id.title, R.id.img });
        setListAdapter(adapter);
    }

    private List<Map<String, Object>> getData() {
        //map.put(参数名字,参数值)
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", "摩托罗拉");
        map.put("img", R.drawable.ic_launcher_foreground);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", "诺基亚");
        map.put("img", R.drawable.ic_launcher_background);
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("title", "三星");
        map.put("img", R.drawable.ic_launcher_foreground);
        list.add(map);
        return list;
    }

}
