package com.xuwd.jvideoplay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class JActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };
    public void initPermission(){
        if(!hasPermissions(VIDEO_PERMISSIONS)){
            confirmPermissions();
            return;
        }
    }
    public boolean hasPermissions(String[]permissions){
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
                        //Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
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
}
