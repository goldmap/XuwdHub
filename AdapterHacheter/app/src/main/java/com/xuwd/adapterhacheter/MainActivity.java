package com.xuwd.adapterhacheter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String activities[]={"Array_Adapter","SimpleListActivity"};
        LinearLayout mainLayout=findViewById(R.id.mainLayout);
        for(int i=0;i<activities.length;i++){
            Button btn=new Button(this);
            btn.setText(activities[i]);
            btn.setId(2000+i);
            btn.setOnClickListener(this);

            mainLayout.addView(btn);
        }
    }

    @Override
    public void onClick(View view) {
        /*
        Button btn=(Button)view;
        String str=btn.getText().toString();
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();*/
        Intent intent=null;
        switch(view.getId()){
            case 2000:
                intent=new Intent(this,ArrayAdapterActivity.class);

        }
        startActivity(intent);
    }
}
