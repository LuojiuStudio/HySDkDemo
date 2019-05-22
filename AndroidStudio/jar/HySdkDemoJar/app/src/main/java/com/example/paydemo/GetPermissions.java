package com.example.paydemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

public class GetPermissions {

    Context context;
    Activity activity;

    static String[] defaultpermissions = {
            Manifest.permission.READ_PHONE_STATE
    };

    public static void permissions(Context context,Activity activity,String[] permissions){
        if (permissions!=null){
            defaultpermissions = defaultpermissions;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // 检查该权限是否已经获取
            for(int i = 0;i < defaultpermissions.length;i++){
                int p  = context.checkSelfPermission( defaultpermissions[i]);
                if (p != PackageManager.PERMISSION_GRANTED ) {
                    activity.requestPermissions(defaultpermissions, 321);
                }
            }
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

        }
    }
    /**
     * 用户权限 申请 的回调方法
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    // 以前是!b
                    boolean b = activity.shouldShowRequestPermissionRationale(String.valueOf(permissions[0]));
                    if (b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle("存储权限不可用123")
                                .setMessage("请在-应用设置-权限-中，允许应用使用存储权限来保存用户数据")
                                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 跳转到应用设置界面
                                        Intent intent = new Intent();

                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                        intent.setData(uri);

                                        activity.startActivityForResult(intent, 123);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
                                    }
                                }).setCancelable(false).show();
                    } else{
                        activity.finish();
                    }
                } else {
                }
            }
        }
    }
}
