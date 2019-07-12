package com.xuwd.jrecycleview.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xuwd.jrecycleview.R;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String demoItems[]={"Linear Recylce","B"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.list_simple_central,R.id.simpleList, demoItems);
        ListView listView=findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        TextView tv=view.findViewById(R.id.simpleList);
        String str= tv.getText().toString();
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
        Intent intent=null;
        switch(position){
            case 0:
                intent=new Intent(this,RecycleLinearActivity.class);
                break;
        }
        if(intent!=null){
            startActivity(intent);
        }
    }
}
