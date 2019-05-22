package com.example.paydemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
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
import com.android.sdk.util.SDKDebug;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends Activity implements OnClickListener,TextWatcher {
    // 鸿延SDK参数
    // 这里要替换成游戏的参数
    private static final String APPID = "666666";
    // 用户游戏内id
    private static final String ROLE_ID = "1231231";
    //角色等级
    private static String ROLE_Level = "0" ;


    public MainActivity(){}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        closeAndroidPDialog();
        initView();
        SDKPay.getInstance(this).onCreate();//本方法必须在onCreate中调用
        GetPermissions.permissions(this,this,null);
    }


        /**
         * 初始化系统控件
         */
    private void initView() {
        findViewById(R.id.inIt).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.createRole).setOnClickListener(this);
        findViewById(R.id.enterRole).setOnClickListener(this);
        findViewById(R.id.pay).setOnClickListener(this);
        findViewById(R.id.createUser).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
        findViewById(R.id.exitGane).setOnClickListener(this);
        EditText editText = findViewById(R.id.RoleLevel);
        editText.addTextChangedListener(this);
    }
/*-------------------This_Demo_Event_Strat---------------------------------------*/
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //获取Demo界面上输入框的值
        ROLE_Level = charSequence.toString();
        SDKDebug.relog("====onTextChanged=============ROLE_Level:"+ROLE_Level);
    }
    @Override
    public void afterTextChanged(Editable editable) {
        ROLE_Level = editable.toString();
        SDKDebug.relog("====afterTextChanged=============ROLE_Level:"+ROLE_Level);
    }
    @Override
    public void onClick(View view) {
        //界面上的所有事件
        int ItemId = view.getId();
        switch (ItemId) {
            case R.id.inIt:         init();         break;
            case R.id.login:        login();        break;
            case R.id.createRole:   cerateRole();   break;
            case R.id.enterRole:    enterRole();    break;
            case R.id.pay:          pay();          break;
            case R.id.createUser:   changeUser();   break;
            case R.id.logout:       logout();       break;
            case R.id.exitGane:     exitGame();     break;
        }
    }
/*-----------------This_Demo_Event_End------------------------------------------*/

/*--------------Hy_Sdk_Fashion_Start--------------------------------------------*/
    /**
     * 初始化
     */
    private void init(){
        InitInfo initInfo = new InitInfo();
        initInfo.setAppid(APPID); // 必填 appid 请联系我们获取
        initInfo.setOrientation(SDKPay.PORTRAIT); // 必填 LANDSCAPE,PORTRAIT
        // 初始化，仅需调用一次，勿重复调用
        SDKPay.getInstance(this).init(initInfo, new InitListener() {
            @Override
            public void initCompleted(int statusCode, InitInfo initInfo) {
                if (PayStatusCode.INIT_SUCCESS == statusCode) {
                    Toast.makeText(getApplicationContext(), "SDK初始化成功！", 1 * 1000).show();
                } else if (PayStatusCode.INIT_FAILED == statusCode) {
                    Toast.makeText(getApplicationContext(), "SDK初始化失败,请检查sdk参数！", 1 * 1000).show();
                }
            }
        });
    }
    /**
     * 调用登录
     */
    private void login(){
        // 登录，在初始化成功后，才可以调用
        // 旧版本SDKPay.getInstance(this).show(this, loginListener);依然可用
        SDKPay.getInstance(this).login(this, loginListener);
    }
    /**
     * 创建角色
     */
    private void cerateRole(){
        // 创建角色时调用
        SDKPay.getInstance(this).createRole(APPID, ROLE_ID);
    }
    /**
     * 上报角色信息
     */
    private void enterRole(){
        // 正式进入游戏游玩界面
        // 此时应上报角色信息
        RoleBean roleBean = new RoleBean();
        // 角色id
        roleBean.setRoleId(ROLE_ID);
        // 角色名
        roleBean.setRoleName("player01");
        // 区服id，最小从1开始（int）
        roleBean.setServerId("1");
        //区服名
        roleBean.setServerName("ceshifu");
        //角色等级
        roleBean.setRoleLevel(ROLE_Level);
        //角色等级改变时间（可选）
        roleBean.setRoleLevelChangeTime("time1");//时间戳
        //角色创建时间(定值)
        roleBean.setRoleCreateDateTime(String.valueOf(System.currentTimeMillis() / 1000));//时间戳
        SDKPay.getInstance(this).enterGame(this, roleBean);
    }
    /**
     * 调用支付
     */
    private void pay(){
        RoleBean roleBeanPay = new RoleBean();
        roleBeanPay.setRoleId(ROLE_ID);
        roleBeanPay.setRoleName("player01");
        // 区服id，最小从1开始
        roleBeanPay.setServerId("1");
        roleBeanPay.setServerName("ceshifu");
        PayInfo payInfo = new PayInfo();
        // CP自定义订单号
        payInfo.setCpOrderId("Test201904171620");
        // 商品名称,需要加上以作标识
        payInfo.setWaresname("测试");
        // 单位：元，微信支付时最低3元,支付宝最低2元,测试时请注意
        payInfo.setPrice("6.00");
        // 游戏appid
        payInfo.setAppid(APPID);
        // 此处的值与roleId一致
        payInfo.setUserid(ROLE_ID);
        // 透传参数
        // 推荐形式为key=value，使用&分割
        payInfo.setExt("key1=value1&key2=value2");
        // 调用支付
        SDKPay.getInstance(this).pay(payInfo, roleBeanPay, payListener);
    }
    /**
     * 切换账号
     */
    private void changeUser(){
        //此方法是游戏主动调用或者是SDk调用0
        loginListener.onLogoutCompleted(PayStatusCode.CHANGE_USER_SUCCESS,null,null);
    }
    /**
     *  登出
     */
    private void logout(){
        //
        SDKPay.getInstance(this).logout(this, loginListener);

    }
    /**
     * 退出游戏
     */
    private void exitGame(){
        // 退出游戏
        RoleBean roleBeanExt = new RoleBean();
        roleBeanExt.setRoleId("001");
        roleBeanExt.setRoleName("player01");
        roleBeanExt.setServerId("1");
        roleBeanExt.setServerName("ceshifu");
        SDKPay.getInstance(this).exitGame(this, roleBeanExt, exitGameListener);
    }
    /**
     * 返回键调用退出
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 必须在回退键按下时退出游戏
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            RoleBean roleBean = new RoleBean();
            roleBean.setRoleId(ROLE_ID);
            roleBean.setRoleName("player01");
            roleBean.setServerId("1");
            roleBean.setServerName("ceshifu");
            SDKPay.getInstance(this).exitGame(this, roleBean, exitGameListener);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
/*--------------Hy_Sdk_Fashion_End-----------------------------------------------*/

/*--------------Callback_Start---------------------------------------------------*/
    //登录、登出，切换用户
    private LoginListener loginListener = new LoginListener() {
        @Override
        public void onLoginCompleted(int statusCode, String account, String userid) {
            // userid是用户唯一标识，可能是数字、字母、下划线组合
            // account可能为空，因此如果在回调中校验，只需要校验userid
            if (PayStatusCode.LOGIN_SUCCESS == statusCode) {
                Toast.makeText(MainActivity.this,"展示:登录成功,用户名:" + account + ", userid:" + userid,Toast.LENGTH_LONG).show();
            } else if (PayStatusCode.LOGIN_FAILED == statusCode) {
                Toast.makeText(MainActivity.this,"展示:登录失败",Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onLogoutCompleted(int statusCode, String account, String userid) {
            // account和userid并不总是返回，请注意
            if (PayStatusCode.CHANGE_USER_SUCCESS == statusCode){
                // 切换角色账号，游戏在在这里做清除当前角色信息，回到登录界面，调起登录等操作
                Toast.makeText(MainActivity.this,"展示:切换用户成功",Toast.LENGTH_LONG).show();
            }else if(PayStatusCode.CHANGE_USER_FAILED == statusCode){
                Toast.makeText(MainActivity.this,"展示:切换用户失败",Toast.LENGTH_LONG).show();
            }else if (PayStatusCode.LOGOUT_SUCCESS == statusCode) {
                Toast.makeText(MainActivity.this,"展示:登出成功,用户名:" + account + ", userid:" + userid,Toast.LENGTH_LONG).show();
                // （可选）此时游戏可以回到主界面，调用登录
                // 请勿在回到主界面的时候再次调用初始化
                // SDKPay.getInstance(this).login(MainActivity.this, loginListener);
            } else if (PayStatusCode.LOGOUT_FAILED == statusCode) {
                Toast.makeText(MainActivity.this,"展示:登出失败",Toast.LENGTH_LONG).show();
            }
        }
    };
    // 支付回调
    private PayListener payListener = new PayListener() {
        @Override
        public void onCompleted(int statusCode, PayInfo payInfo) {
            if (PayStatusCode.PAY_SUCCESS == statusCode) {
                Toast.makeText(getApplicationContext(), "支付成功！", 1 * 1000).show();
            } else if (PayStatusCode.PAY_CANCEL == statusCode) {
                Toast.makeText(getApplicationContext(), "用户取消支付！", 1 * 1000).show();
            } else if (PayStatusCode.ERROR_PAY_FAILED == statusCode) {
                Toast.makeText(getApplicationContext(), "支付失败！", 1 * 1000).show();
            } else if (PayStatusCode.PAY_UNKNOWN == statusCode) {
                // 鸿延sdk后端没有收到支付消息或者内部错误
                Toast.makeText(getApplicationContext(), "支付结未知！等待服务器通知", 1 * 1000).show();
            }
        }
    };

    //退出接口回调
    private ExitGameListener exitGameListener = new ExitGameListener() {
        @Override
        public void onPendingExit(int result) {
            if (PayStatusCode.EXIT_GAME == result) {
                // 离开游戏
                // sdk不会主动结束游戏，请在必要处理后手动z结束游戏
                MainActivity.this.finish();
            } else {
                // 用户取消离开
            }
        }
    };
/*--------------Callback_End----------------------------------------------------*/
/*--------------Activity_Life_Cycle_Start---------------------------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        SDKPay.getInstance(this).onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        SDKPay.getInstance(this).onResume();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        SDKPay.getInstance(this).onRestart();
    }
    @Override
    protected void onPause() {
        super.onPause();
        SDKPay.getInstance(this).onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        SDKPay.getInstance(this).onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SDKPay.getInstance(this).onDestroy();
    }
/*--------------Activity_Life_Cycle_End---------------------------------------*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SDKPay.getInstance(this).onBackPressed();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKPay.getInstance(this).onNewIntent(intent);
    }
    private void closeAndroidPDialog(){
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
