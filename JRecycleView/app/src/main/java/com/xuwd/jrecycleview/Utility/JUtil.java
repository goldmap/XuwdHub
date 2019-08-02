package com.xuwd.jrecycleview.Utility;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JUtil {

    public static String getStoragePath(Context mContext, boolean is_removable) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> volumList=mStorageManager.getStorageVolumes();

        try {
            Method getPath= StorageVolume.class.getMethod("getPath");

            for(int i=0;i<volumList.size();i++){
                StorageVolume storageVolume=volumList.get(i);
                String path=(String) getPath.invoke(storageVolume);
                boolean removable=storageVolume.isRemovable();
                if(is_removable==removable)
                    return path;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class FileItem {
        public String fileName;
        public String filePath;
        public boolean isZip;

        public FileItem(){

        }

        public FileItem(String fileName, String filePath, boolean isZip) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.isZip = isZip;
        }
    }
}
