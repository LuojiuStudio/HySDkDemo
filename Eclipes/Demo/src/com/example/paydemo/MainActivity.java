package com.example.paydemo;

import com.android.sdk.port.ExitGameListener;
import com.android.sdk.port.InitInfo;
import com.android.sdk.port.InitListener;
import com.android.sdk.port.LoginListener;
import com.android.sdk.port.PayInfo;
import com.android.sdk.port.PayStatusCode;
import com.android.sdk.port.PayListener;
import com.android.sdk.port.RoleBean;
import com.android.sdk.port.SDKPay;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	// 鸿延SDK参数
	// 这里要替换成游戏的参数
	private static final String APPID = "666666";
	// 用户游戏内id
	private static final String ROLE_ID = "445249018300";
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
				Toast.makeText(getApplicationContext(), "支付结果未知！等待服务器通知", 1 * 1000).show();
			}
		}
	};

	// 登录接口回调
	private LoginListener loginListener = new LoginListener() {
		@Override
		public void onLoginCompleted(int statusCode, String account, String userid) {
			// userid是用户唯一标识，可能是数字、字母、下划线组合
			// account可能为空，因此如果在回调中校验，只需要校验userid
			if (PayStatusCode.LOGIN_SUCCESS == statusCode) {
				
				Toast.makeText(MainActivity.this, "展示:登录成功,用户名:" + account + ", userid:" + userid, Toast.LENGTH_LONG)
						.show();
			} else if (PayStatusCode.LOGIN_FAILED == statusCode) {
				Toast.makeText(MainActivity.this, "展示:登录失败", Toast.LENGTH_LONG).show();
			}
		}
		@Override
		public void onLogoutCompleted(int statusCode, String account, String userid) {
			// account和userid并不总是返回，请注意
			if(PayStatusCode.CHANGE_USER_SUCCESS == statusCode) {
				//渠道SDk触发的切换账号，游戏应该这里做清除游戏数据、回到登录界面、调起登录框等操作
				Toast.makeText(MainActivity.this, "切换账号", Toast.LENGTH_LONG).show();
			}else if(PayStatusCode.CHANGE_USER_FAILED == statusCode) {
				Toast.makeText(MainActivity.this, "切换账号失败", Toast.LENGTH_LONG).show();
			}else if (PayStatusCode.LOGOUT_SUCCESS == statusCode) {
				// （可选）此时游戏可以回到主界面，调用登录
				// 请勿在回到主界面的时候再次调用初始化
				Toast.makeText(MainActivity.this, "展示:登出成功,用户名:" + account + ", userid:" + userid, Toast.LENGTH_LONG).show();
			} else if (PayStatusCode.LOGOUT_FAILED == statusCode) {
				Toast.makeText(MainActivity.this, "展示:登出失败", Toast.LENGTH_LONG).show();
			}
		}
	};

	private ExitGameListener exitGameListener = new ExitGameListener() {
		@Override
		public void onPendingExit(int result) {
			if (PayStatusCode.EXIT_GAME == result) {
				// 离开游戏
				// sdk不会主动结束游戏，请必须在这里手动结束游戏
				MainActivity.this.finish();
			} else {
				// 用户取消离开
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 主Activity.onCreate回调中调用
		SDKPay.getInstance(this).onCreate();
		initView();
	}

	private void initView() {
		//接入方不需要关注这段代码
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		Button initButton = new Button(this);
		initButton.setText("init");
		initButton.setTag("init");
		initButton.setOnClickListener(this);

		Button loginButton = new Button(this);
		loginButton.setText("login");
		loginButton.setTag("login");
		loginButton.setOnClickListener(this);
      
		Button createRoleButton = new Button(this);
		createRoleButton.setText("createRole");
		createRoleButton.setTag("createRole");
		createRoleButton.setOnClickListener(this);

		Button enterGameButton = new Button(this);
		enterGameButton.setText("enterGame");
		enterGameButton.setTag("enterGame");
		enterGameButton.setOnClickListener(this);

		Button payButton = new Button(this);
		payButton.setText("pay");
		payButton.setTag("pay");
		payButton.setOnClickListener(this);
		
		Button changeUser = new Button(this);
		changeUser.setText("changeUser");
		changeUser.setTag("changeUser");
		changeUser.setOnClickListener(this);
		
		
		Button logoutButton = new Button(this);
		logoutButton.setText("logout");
		logoutButton.setTag("logout");
		logoutButton.setOnClickListener(this);

		Button exitGameButton = new Button(this);
		exitGameButton.setText("exitGame");
		exitGameButton.setTag("exitGame");
		exitGameButton.setOnClickListener(this);

		layout.addView(initButton);
		layout.addView(loginButton);
		layout.addView(createRoleButton);
		layout.addView(enterGameButton);
		layout.addView(payButton);
		layout.addView(changeUser);
		layout.addView(logoutButton);
		layout.addView(exitGameButton);
		setContentView(layout);

		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		String imsi = tm.getSubscriberId();

		if (!TextUtils.isEmpty(imsi)) {
			TextView tvImsi = new TextView(this);
			tvImsi.setText("\t\tIMSI:\t" + imsi);
			layout.addView(tvImsi);
		}
		if (!TextUtils.isEmpty(imei)) {
			TextView tvImei = new TextView(this);
			tvImei.setText("\t\tIMEI:\t" + imei);
			layout.addView(tvImei);
		}
		setContentView(layout);
	}

	@Override
	public void onClick(View v) {
		// 【重要】以下罗列的接口如无特殊说明，都为必须接口

		if (v.getTag().equals("init")) { 
			// 鸿延SDK初始化信息
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
		} else if (v.getTag().equals("login")) {
			// 登录，在初始化成功后，才可以调用
			// 旧版本SDKPay.getInstance(this).show(this, loginListener);依然可用 
			SDKPay.getInstance(this).login(this, loginListener);
		} else if (v.getTag().equals("createRole")) {
			// 创建角色时调用
			SDKPay.getInstance(this).createRole(APPID, ROLE_ID);
		} else if (v.getTag().equals("enterGame")) {
			// 正式进入游戏游玩界面
			// 此时应上报角色信息
			RoleBean roleBean = new RoleBean();
			// 角色id(int)
			roleBean.setRoleId(ROLE_ID);
			// 角色名（String）
			roleBean.setRoleName("player01");
			// 角色的等级（int）
			roleBean.setRoleLevel("1");
			//角色创建的时间,一个定值,服务器里存储的值(时间戳)
			//角色等级改变时间（可选）
			roleBean.setRoleLevelChangeTime("time1");//时间戳
			String CreateDate = String.valueOf(System.currentTimeMillis() / 1000);
			roleBean.setRoleCreateDateTime(CreateDate);
			// 区服id，最小从1开始(int)
			roleBean.setServerId("1");
			// 区服名(Strig)
			roleBean.setServerName("ceshifu");
			SDKPay.getInstance(this).enterGame(this, roleBean);
		} else if (v.getTag().equals("pay")) {
			// 支付
			RoleBean roleBean = new RoleBean();
			roleBean.setRoleId(ROLE_ID);
			roleBean.setRoleName("player01");
			// 区服id，最小从1开始
			roleBean.setServerId("1");
			// 区服名称
			roleBean.setServerName("ceshifu");
			PayInfo payInfo = new PayInfo();
			// CP自定义订单号
			payInfo.setCpOrderId("CPTEST000029");
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
			SDKPay.getInstance(this).pay(payInfo, roleBean, payListener);
		}else if(v.getTag().equals("changeUser"))  {
			  loginListener.onLogoutCompleted(PayStatusCode.CHANGE_USER_SUCCESS,null,null);
		}else if (v.getTag().equals("logout")) {
			// 登出
			SDKPay.getInstance(this).logout(this, loginListener);
		} else if (v.getTag().equals("exitGame")) {
			// 退出游戏
			RoleBean roleBean = new RoleBean();
			roleBean.setRoleId("001");
			roleBean.setRoleName("player01");
			roleBean.setServerId("1");
			roleBean.setServerName("ceshifu");
			SDKPay.getInstance(this).exitGame(this, roleBean, exitGameListener);
		}
	}

	// 生命周期回调
	// 【重要】全部必须，不可遗漏

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
}