package com.yqdq.yixu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.android.sdk.port.ExitGameListener;
import com.android.sdk.port.InitInfo;
import com.android.sdk.port.InitListener;
import com.android.sdk.port.LoginListener;
import com.android.sdk.port.PayInfo;
import com.android.sdk.port.PayListener;
import com.android.sdk.port.PayStatusCode;
import com.android.sdk.port.RoleBean;
import com.android.sdk.port.SDKPay;

import org.json.JSONException;
import org.json.JSONObject;

/***
 * @author prony
 */
public class MainActivity extends Activity {

    //WebView 容器
    X5WebView mWebView;

    // 鸿延SDK参数
    // 这里要替换成游戏的参数
    private static final String APPID = "1912217";
    private static String ROLE_ID;

    // 支付回调
    private PayListener payListener = new PayListener() {
        @Override
        public void onCompleted(int statusCode, PayInfo payInfo) {
            if (PayStatusCode.PAY_SUCCESS == statusCode) {

            } else if (PayStatusCode.PAY_CANCEL == statusCode) {

            } else if (PayStatusCode.ERROR_PAY_FAILED == statusCode) {

            } else if (PayStatusCode.PAY_UNKNOWN == statusCode) {
                // 鸿延sdk后端没有收到支付消息或者内部错误

            }
            //只有0为成功 其他都为失败
            callJsMethod("onPayResult", String.valueOf(statusCode));
        }
    };

    // 登录接口回调
    private LoginListener loginListener = new LoginListener() {

        @Override
        public void onLoginCompleted(int statusCode, String account, String userid) {
            // userid是用户唯一标识，可能是数字、字母、下划线组合
            // account可能为空，因此如果在回调中校验，只需要校验userid
            if (PayStatusCode.LOGIN_SUCCESS == statusCode) {


            } else if (PayStatusCode.LOGIN_FAILED == statusCode) {

            }
            //只有0为成功 其他都为失败
            callJsMethod("onloginComplete", "{\"code\":" + statusCode + ",\"userId\":\"" + userid + "\"}");
        }

        @Override
        public void onLogoutCompleted(int statusCode, String account, String userid) {
            // account和userid并不总是返回，请注意
            if (PayStatusCode.CHANGE_USER_SUCCESS == statusCode){
                // 切换角色账号，游戏在在这里做清除当前角色信息，回到登录界面，调起登录等操作
                // 可以主动调用loginListener.onLogoutCompleted(PayStatusCode.CHANGE_USER_SUCCESS,null,null);进行测试
            }else if(PayStatusCode.CHANGE_USER_FAILED == statusCode){
                //切换登录失败
            }else if (PayStatusCode.LOGOUT_SUCCESS == statusCode) {
                // （可选）此时游戏可以回到主界面，调用登录
                // 请勿在回到主界面的时候再次调用初始化
                // SDKPay.getInstance(this).login(MainActivity.this, loginListener);
                exitDialog("即将退出游戏注销!");
            } else if (PayStatusCode.LOGOUT_FAILED == statusCode) {

            }

            //只有0为成功 其他都为失败
            callJsMethod("onlogoutComplete", String.valueOf(statusCode));
        }
    };

    private ExitGameListener exitGameListener = new ExitGameListener() {

        @Override
        public void onPendingExit(int result) {
            if (PayStatusCode.EXIT_GAME == result) {
                // 离开游戏
                // 注意:sdk不会主动结束游戏，请在必要处理后手动z结束游戏
                System.exit(0);
            } else {
                // 用户取消离开
            }
        }
    };


    private void exitDialog(String message){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exitGameListener.onPendingExit(0);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKPay.getInstance(this).onCreate();
        setContentView( R.layout.game_activity_main);
        sdkInit();
        setmWebView();
    }

    public void setmWebView() {
        mWebView = (X5WebView) findViewById( R.id.main_container);
        mWebView.initWebViewSettings(getApplicationContext().getCacheDir().getAbsolutePath());
        if(mWebView.getX5WebViewExtension()==null){
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("file:///android_asset/test.html");
            }
        });
        //mWebView.loadUrl("file:///android_asset/test.html");
        mWebView.addJavascriptInterface(new SDKPayJsInterFace(), "SDKPay");

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void callJsMethod(String MethodName, String arg) {
        mWebView.evaluateJavascript("javascript:" + MethodName + "(" + arg + ")", new com.tencent.smtt.sdk.ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitDialog("退出游戏？");
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }


    private void sdkInit() {
        // 鸿延SDK初始化信息
        InitInfo initInfo = new InitInfo();
        initInfo.setAppid(APPID); // 必填 appid 请联系我们获取
        initInfo.setOrientation(SDKPay.PORTRAIT); // 必填 LANDSCAPE,PORTRAIT
        // 初始化，仅需调用一次，勿重复调用
        SDKPay.getInstance(MainActivity.this).init(initInfo, new InitListener() {

            @Override
            public void initCompleted(int statusCode, InitInfo initInfo) {
                if (PayStatusCode.INIT_SUCCESS == statusCode) {
                    MainActivity.this.findViewById(R.id.splash).setVisibility(View.GONE);
                } else if (PayStatusCode.INIT_FAILED == statusCode) {
                    Toast.makeText(MainActivity.this,"初始化失败",Toast.LENGTH_SHORT).show();
                }
                callJsMethod("onInitComplete", String.valueOf(statusCode));
            }
        });

    }

    //js接口内部类
    public class SDKPayJsInterFace {

        @JavascriptInterface
        public void init() {
                sdkInit();
        }

        @JavascriptInterface
        public void login() {
            // 登录，在初始化成功后，才可以调用
            // 旧版本SDKPay.getInstance(this).show(this, loginListener);依然可用
            SDKPay.getInstance(MainActivity.this).login(MainActivity.this, loginListener);
        }

        @JavascriptInterface
        public void create_role(String roleId) {
            ROLE_ID = roleId;
            // 创建角色时调用
            SDKPay.getInstance(MainActivity.this).createRole(APPID, roleId);
        }

        @JavascriptInterface
        public void enterGame(String roleInfo) {
            // 正式进入游戏游玩界面
            // 此时应上报角色信息
            RoleBean roleBean = getRoleBean(roleInfo);
            SDKPay.getInstance(MainActivity.this).enterGame(MainActivity.this, roleBean);
        }

        @JavascriptInterface
        public void pay(String strPayInfo, String roleInfo) {
            // 支付
            RoleBean roleBean = getRoleBean(roleInfo);
            PayInfo payInfo = getPayInfo(strPayInfo);
            payInfo.setAppid(APPID);
            payInfo.setUserid(ROLE_ID);
            // 调用支付
            SDKPay.getInstance(MainActivity.this).pay(payInfo, roleBean, payListener);
        }

        @JavascriptInterface
        public void logout() {
            // 登出
            SDKPay.getInstance(MainActivity.this).logout(MainActivity.this, loginListener);
        }
    }


    public PayInfo getPayInfo(String jsonStr) {
        PayInfo payInfo = new PayInfo();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            // CP自定义订单号
            payInfo.setCpOrderId(jsonObject.getString("CpOrderId"));
            // 商品名称,需要加上以作标识
            payInfo.setWaresname(jsonObject.getString("Waresname"));
            // 单位：元，微信支付时最低3元,支付宝最低2元,测试时请注意
            payInfo.setPrice(jsonObject.getString("Price"));
            // 游戏appid
            payInfo.setAppid(APPID);
            // 此处的值与roleId一致
            payInfo.setUserid(ROLE_ID);
            // 透传参数
            // 推荐形式为key=value，使用&分割
            payInfo.setExt(jsonObject.getString("Ext"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payInfo;
    }

    public RoleBean getRoleBean(String roleInfo) {
        RoleBean roleBean = new RoleBean();
        try {
            JSONObject jsonObject = new JSONObject(roleInfo);
            roleBean.setRoleName(jsonObject.getString("RoleName"));//string
            roleBean.setServerId(jsonObject.getString("ServerID"));//int
            roleBean.setServerName(jsonObject.getString("ServerName"));//string
            roleBean.setRoleLevel(jsonObject.getString("roleLevel"));//int
            roleBean.setRoleCreateDateTime(jsonObject.getString("roleCreateDateTime"));//时间戳
            roleBean.setRoleLevelChangeTime(jsonObject.getString("roleLevelChangeTime"));//时间戳
            roleBean.setRoleId(ROLE_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return roleBean;
    }

    /**
     * 通过Activity管理XWalkWebView的声明周期
     */
    @Override
    protected void onPause() {
        if(mWebView!=null){
            mWebView.onPause();
        }
        SDKPay.getInstance(this).onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(mWebView!=null){
            mWebView.onResume();
        }
        SDKPay.getInstance(this).onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        SDKPay.getInstance(this).onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        SDKPay.getInstance(this).onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {

        SDKPay.getInstance(this).onStart();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        SDKPay.getInstance(this).onRestart();
        super.onRestart();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKPay.getInstance(this).onActivityResult(requestCode, requestCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKPay.getInstance(this).onNewIntent(intent);

    }
}
