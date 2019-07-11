package com.xuwd.jrecycleview;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity extends AdapterView.OnItemClickListener{
    private String demos[]={"A","B"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.list_simple_central,R.id.simpleList, demos);
        ListView listView=findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }
}
