package com.yqdq.yixu;

import com.android.sdk.port.HYApplication;
import com.android.sdk.util.SDKDebug;
import com.tencent.smtt.sdk.QbSdk;

public class H5Application extends HYApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {

            }

            @Override
            public void onCoreInitFinished() {
                SDKDebug.dlog("X5 kernel init success");
            }
        };
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }
}
