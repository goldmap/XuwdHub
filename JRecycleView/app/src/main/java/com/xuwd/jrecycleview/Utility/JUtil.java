package com.xuwd.jrecycleview.Utility;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.util.ArrayList;
import java.util.List;

public class JUtil {

    public static String getStoragePath(Context mContext, boolean is_removable) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> volumList=mStorageManager.getStorageVolumes();
        for(int i=0;i<volumList.size();i++){
            StorageVolume storageVolume=volumList.get(i);
        }
        return "";
    }
}
