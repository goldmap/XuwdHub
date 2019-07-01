package com.xuwd.xcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTest=findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Test();
            }
        });

        final Context context=this;
        Button btn=findViewById(R.id.btnCamera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(VIDEO_PERMISSIONS)){
                    Toast.makeText(context,"权限有待开通",Toast.LENGTH_SHORT).show();
                    confirmPermissions();
                    return;
                }
                Toast.makeText(context,"权限OK",Toast.LENGTH_SHORT).show();
//                getSupportFragmentManager().beginTransaction().replace(R.id.mainLayout,new CameraFragment()).commit();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(android.R.id.content, new CameraFragment());
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);//设置动画效果，magic!!!
                ft.commit();
            }
        });

    }

    private boolean hasPermissions(String[]permissions){
        for(String permission:permissions){
            if(ActivityCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;//所有的权限都开通，则返回true
    }
    private void confirmPermissions(){
        //坑点：有的权限在之前被赋予“不再询问”，无法用requesePemission函数喊醒，需要检查并提醒人工设置。
        if(noPrompt(VIDEO_PERMISSIONS)){
            Toast.makeText(this,"需要通过手机【设置】取得权限",Toast.LENGTH_SHORT).show();
        }else {
            requestPermissions(VIDEO_PERMISSIONS,REQUEST_VIDEO_PERMISSIONS);
        }
    }
    private boolean noPrompt(String[] permissions){
        for(String permission:permissions){
            if(shouldShowRequestPermissionRationale(permission)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int i=0;i<grantResults.length;i++) {
                    int result=grantResults[i];
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        //ErrorDialog.newInstance(getString(R.string.permission_request)).show(getFragmentManager(), FRAGMENT_DIALOG);
                        new ConfirmationDialog().show(getSupportFragmentManager(),"");
                    }
                    else{
                        String str=permissions[i]+"权限OK";
                        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                ErrorDialog.newInstance("设置错误").show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static class ConfirmationDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("所需要的权限未获得，重新设置权限")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(VIDEO_PERMISSIONS,REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(),"未取得必要权限，无法正常运行",Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }
    private void Test(){
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Toast.makeText(this,"Rot:"+rotation,Toast.LENGTH_SHORT).show();
    }
}
