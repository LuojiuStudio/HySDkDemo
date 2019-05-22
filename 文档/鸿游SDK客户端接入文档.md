## 版本
文档对应版本1.1.6。

版本号 | 修改日期 | 备注
---|---|---
1.0.11 | 2017.03 | 
1.0.12 | 2017.03 | 
1.0.13 | 2017.04 | 
1.0.14 | 2017.04 | 无sdk接口强制改动 
1.0.14b | 2017.04 | 修复SYZ闪屏小BUG
1.0.15 | 2017.04 |
1.1.0 | 2017.05 | 重大更新，解决重复出包
1.1.1 | 2017.05 | 修复资源冲突
1.1.2 | 2017.05 | 优化部分代码
1.1.3 | 2017.05 | 优化支付
1.1.4 | 2017.06 | 修复若干问题
1.1.5 | 2017.11 | 优化闪屏、支付与部分接口
1.1.5b | 2017.11 | 修复若干问题
1.1.6 | 2018.3 | 内部代码优化
1.1.6.4 | 2019.5.5 | RoleBean类增加`setRoleLevel(level)`等方法、增加切换账户方案

## 前言
本文档供接入鸿游SDK、具有安卓客户端相关开发经验的技术人员参考。

## 测试账号
平台有平台币支付方式，需要的测试人员可以登录如下账号：

账号：15221929675

密码：33523294

但强烈建议出包前测试支付宝与微信支付，尽早避免问题。

## 接入步骤

### 1.接入前须知

请确保WorkSpace编码是UTF-8，否则可能会出现反射获取不到资源的问题。

修改主工程AndroidManifest.xml，targetSdkVersion为(17&lt;=targetSdkVersion&lt;=21)的范围。

修改主工程AndroidManifest.xml，在调用sdk登录和支付模块的Activity中，加入如下设置: 

> android:configChanges="screenSize|orientation|keyboard|navigation|layoutDirection"

以防可能出现的横竖屏切换时多次调用生命周期的相关方法。

### 2.拷贝文件(依赖本工程)
将 DEMO 工程中assets下的文件复制到您的工程中，    
并添加压缩包中项目Dependent_HY为依赖工程，   
将Demo工程中 AndroidManifest.xml 中的相关权限和声明拷贝到您的工程 manifest 中。


注意在AndroidManifest.xml中，如果您的游戏没有自定义application，   
请直接使用`com.android.sdk.port.HYApplication`；   
如果已经有application，请继承`com.android.sdk.port.HYApplication`。

将sdk压缩包根目录下hyinfo.store复制到游戏工程assets目录下。   
修改其中hyinfo.store文件中`"mergeAppId":""`的值为Appid，   
例如appid如果为'666666'，   
则改为`"mergeAppId":"666666"`，  
注意，DEMO中的hyinfo.store对应appid为666666，这是演示用appid；   
而根目录下hyinfo.store对应您游戏的appid。如果压缩包中没有hyinfo.store，请联系我们获取。   

> **注意：** 请至少保证libs/armeabi下有完整的游戏所需so文件。由于安卓的机制，各指令集（armeabi、armeabi-v7a、armeabi-v8a、x86）下的so文件个数必须一一对应，否则游戏会在某些机型崩溃。由于鸿游sdk仅提供armeabi的so文件，因此请保证armeabi有完整游戏所需so，鸿游会在拿到游戏包后删除其他指令集（所有构架兼容armeabi）。

## 客户端接口和生命周期回调

> **注意：** 如无特殊说明，以下接口和生命周期回调都为必须接口。

### 3.初始化
初始化时调用
```java
SDKPay.getInstance(this).init(initInfo,initListener)
```
其他接口（登录支付等）需要在初始化成功后调用。   
**若初始化报错，请及时与我们联系**
```java
SDKPay.getInstance(this).init(initInfo, new InitListener() {
	@Override
	public void initCompleted(int statusCode, InitInfo initInfo) {
		if (PayStatusCode.INIT_SUCCESS == statusCode) {
			//初始化成功
		} else if (PayStatusCode.INIT_FAILED == statusCode) {
			//初始化失败
		}
	}
});
```



### 4.登录/登出/切换账户
#### 登录时请调用
```java
SDKPay.getInstance(this).login(activity,loginListener)
```
来方法展示登录界面，登录成功后我方会将账号(account)和用户编号(userid)以回调形式返回。

userid可能是数字、字母、下划线组合。

需要注意，account并不一定返回，因此如果需要校验返回结果，请只校验userid。

当登录失败或者用户点击返回键关闭登陆框后，点击游戏内按钮，应当能重新调用登录框。   

#### 登出时请调用
```java
SDKPay.getInstance(this).logout(this,loginListener);
```
#### LoginListener回调   
```java
// 登录接口回调
private LoginListener loginListener = new LoginListener() {
	 @Override
        public void onLoginCompleted(int statusCode, String account, String userid) {
            // userid是用户唯一标识，可能是数字、字母、下划线组合
            // account可能为空，因此如果在回调中校验，只需要校验userid
            if (PayStatusCode.LOGIN_SUCCESS == statusCode) {
                //登录成功
            } else if (PayStatusCode.LOGIN_FAILED == statusCode) {
                //登录失败
            }
        }
        @Override
        public void onLogoutCompleted(int statusCode, String account, String userid) {
            // account和userid并不总是返回，请注意
            if (PayStatusCode.CHANGE_USER_SUCCESS == statusCode){
                //切换账户
                //渠道SDk触发的切换账号，游戏应该这里做清除游戏数据、回到登录界面、调起登录框等操作
            }else if(PayStatusCode.CHANGE_USER_FAILED == statusCode){
                //切换账户失败
            }else if (PayStatusCode.LOGOUT_SUCCESS == statusCode) {
                // 登出成功
                //（可选）此时游戏可以回到主界面，调用登录
                // 请勿在回到主界面的时候再次调用初始化
                // SDKPay.getInstance(this).login(MainActivity.this, loginListener);
            } else if (PayStatusCode.LOGOUT_FAILED == statusCode) {
                //登出失败
            }
        }
};
```

### 5.创建角色
如您的游戏需要用户创建角色，请在创建完成后调用
```java
SDKPay.getInstance(this).createRole(APPID, USERID)
```
(每个新角色仅调用一次)，如不需要用户手动创建角色，可以在创建用户信息时调用 createRole 方法，   
以便我们统计用户信息，在后台展示给游戏内容提供商。

### 6.进入游戏（提交角色信息）
使用以下方法提交角色信息，通知sdk进入游戏。   
注:提交角色信息必须在进入游戏、等级变化时调用
```java
RoleBean roleBean = new RoleBean();
roleBean.setRoleId("001");//角色id
roleBean.setRoleName("player01");//角色名称
roleBean.setRoleLevel("1");//角色等级
roleBean.setServerId("1");//服务器id
roleBean.setServerName("ceshifu");//服务器名
roleBean.setRoleLevelChangeTime("");//角色等级改变时间
roleBean.setRoleCreateDateTime("");//角色创建时间，必填
//提交角色信息
SDKPay.getInstance(this).enterGame(activity,roleBean)
```

 RoleBean                  |参数                            |类型
---------------------------|-------------------------------|---
setRoleId("");             | 角色ID						    |int
setRoleName("");           | 角色名称					    |String
setRoleLevel("");          | 角色等级                       |int
setServerId("");           | 服务器ID					    |int
setServerName("");         | 服务器名称					    |String
setRoleLevelChangeTime("");| 角色等级改变时间**变值**(必填)  |String (时间戳)
setRoleCreateDateTime(""); | 角色创建时间**定值**可选参数    |String (时间戳)


### 7.支付
支付时请调用   
```java
SDKPay.getInstance(this).pay(payInfo,payListener)
```
PayListener为支付结果回调，所需参数：

```java
RoleBean roleBean = new RoleBean();
roleBean.setRoleId("001");//角色id
roleBean.setRoleName("player01");//角色名称
roleBean.setRoleLevel("1"); //角色等级
roleBean.setServerId("1");//服务器id
roleBean.setServerName("ceshifu");//服务器名
PayInfo payInfo = new PayInfo();
String orderId = "CP0000";
payInfo.setCpOrderId(orderId);//CP自定义订单号
payInfo.setWaresname("测试");//商品名称,需要加上以作标识
payInfo.setPrice("3.00");//单位：元，微信支付时最低3元,支付宝最低2元,测试时请注意
payInfo.setAppid(APPID);//游戏appid
payInfo.setUserid(USERID);//角色id，与roleId一致
payInfo.setExt("你的透传参数");//透传参数形式推荐key=value，使用&分割，不能包含"|"、"&"字符
//调用支付
SDKPay.getInstance(this).pay(payInfo, roleBean,payListener);
```

### 8.退出游戏
#### 退出游戏调用
```java
SDKPay.getInstance(this).exitGame(this, roleBean, exitGameListener)
```

```java
RoleBean roleBean = new RoleBean();
roleBean.setRoleId("001");
roleBean.setRoleName("player01");
roleBean.setServerId("1");
roleBean.setServerName("ceshifu");
//退出游戏
SDKPay.getInstance(this).exitGame(this, roleBean, exitGameListener);
```
#### ExitGameListener回调
```java
private ExitGameListener exitGameListener = new ExitGameListener() {
	@Override
	public void onPendingExit(int result) {
		if (PayStatusCode.EXIT_GAME == result) {
			//离开游戏
			//sdk不会主动结束游戏，为确保退出游戏，请在必要处理后手动结束游戏
			MainActivity.this.finish();
			//android.os.Process.killProcess(android.os.Process.myPid());
			//System.exit(0);
		} else {
			//用户取消离开
		}
	}
};
```
#### 返回键(必须在点击返回键的时候调用此方法)
```java
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
    	RoleBean roleBean = new RoleBean();
		roleBean.setRoleId("001");
		roleBean.setRoleName("player01");
		roleBean.setServerId("1");
		roleBean.setServerName("ceshifu");
    	SDKPay.getInstance(this).exitGame(this, roleBean);
        return true;
    }
    return super.onKeyDown(keyCode, event);
}
```

### 9.生命周期回调
请参考demo，在主Activity中调用：
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	SDKPay.getInstance(this).onCreate();
}

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
public void onBackPressed() {
	super.onBackPressed();
	SDKPay.getInstance(this).onBackPressed();
}

@Override
protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	SDKPay.getInstance(this).onNewIntent(intent);
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

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	SDKPay.getInstance(this).onActivityResult(requestCode, resultCode, data);
}
```





## 混淆
我们强烈建议不要混淆除了游戏以外的任何代码，否则sdk将无法工作。

## 交付
无闪屏、包名和icon要求。

## 其他
接入后微信、支付宝、开通银行卡支付方式。

建议游戏内容提供商参考sdk返回支付结果和我们服务器后端回调结果发放道具，您需要接收服务器回调验证和记录，请将回调地址告知我们，以便我们将支付结果通过回调方式传回。

如有其他问题请参考Demo工程或联系我们。

