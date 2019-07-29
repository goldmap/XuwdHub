package com.xuwd.jrecycleview.Utility;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.util.ArrayList;
import java.util.List;

public class JUtil {

    public static String getStoragePath(Context mContext, boolean is_removale) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumes= mStorageManager.getStorageVolumes();
        final int length = storageVolumes.size();
        for (int i = 0; i < length; i++) {
            Object storageVolumeElement = storageVolumes.get(i);
            String path = (String) storageVolumeElement.getPath();
           // boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
            if (is_removale == removable) {
                return path;
            }
        }
        return "";
    }
}
