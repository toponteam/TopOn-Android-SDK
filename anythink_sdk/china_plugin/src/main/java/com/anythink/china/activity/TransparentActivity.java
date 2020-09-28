package com.anythink.china.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.anythink.china.common.PermissionRequestManager;

import java.util.concurrent.ConcurrentHashMap;

public class TransparentActivity extends Activity {

    public static final String TYPE = "type";
    public static final String REQUEST_CODE_KEY = "request_code";

    /**
     * For permission
     **/
    public final static int PERMISSION_HANDLE_TYPE = 1000;
    public final static String PERMISSION_LIST = "permission_list";
    public static final ConcurrentHashMap<Integer, PermissionRequestManager.PermissionAuthorizeCallback> permissionMap = new ConcurrentHashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE, 0);
        if (type == PERMISSION_HANDLE_TYPE) {
            String[] permissionList = intent.getStringArrayExtra(PERMISSION_LIST);
            int requestCode = intent.getIntExtra(REQUEST_CODE_KEY, 0);
            ActivityCompat.requestPermissions(this,
                    permissionList, requestCode);
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_HANDLE_TYPE) {
            for (int i = 0; i < permissions.length; i++) {
//                Log.i("ZSR", permissions[i] + ":" + grantResults[i]);
            }
        }
        PermissionRequestManager.PermissionAuthorizeCallback callback = permissionMap.get(requestCode);
        if (callback != null) {
            callback.onResultCallback(permissions, grantResults);
            permissionMap.remove(requestCode);
        }

        finish();
    }
}
