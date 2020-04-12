package com.jarchie.jsbridge;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者: 乔布奇
 * 日期: 2020-04-11 13:50
 * 邮箱: jarchie520@gmail.com
 * 描述: Native端页面展示层，上方为Web页面，下方为Android页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView mWebview;
    private EditText mEditText;
    private TextView mShowBtn, mShowBtn2, mRefreshWeb;
    private NativeSDK nativeSDK = new NativeSDK(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        initData();
    }

    //初始化数据加载
    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void initData() {
        mWebview.loadUrl("http://192.168.0.102:8080/?timestamp" + new Date().getTime());
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new MyWebChromeClient());
        mWebview.addJavascriptInterface(new NativeBridge(this), "NativeBridge");
    }

    //初始化事件监听
    private void initListener() {
        mShowBtn.setOnClickListener(this);
        mShowBtn2.setOnClickListener(this);
        mRefreshWeb.setOnClickListener(this);
    }

    //初始化绑定控件
    private void initView() {
        mWebview = findViewById(R.id.mWebview);
        mEditText = findViewById(R.id.mEditText);
        mShowBtn = findViewById(R.id.mShowBtn);
        mShowBtn2 = findViewById(R.id.mShowBtn2);
        mRefreshWeb = findViewById(R.id.mRefreshWeb);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mShowBtn:
                showWebDialog(mEditText.getText().toString().trim());
                break;
            case R.id.mShowBtn2:
                nativeSDK.getWebEditTextValue(new Callback() {
                    @Override
                    public void invoke(String value) {
                        showNativeDialog2("Web 输入值：" + value);
                    }
                });
                break;
            case R.id.mRefreshWeb:
                mWebview.loadUrl("http://192.168.0.102:8080/?timestamp" + new Date().getTime());
                break;
        }
    }

    interface Callback {
        void invoke(String value);
    }

    class NativeSDK {
        private Context mContext;
        private int id = 1;
        private Map<Integer, Callback> callbackMap = new HashMap();

        NativeSDK(Context context) {
            this.mContext = context;
        }

        void getWebEditTextValue(Callback callback) {
            int callbackId = id++;
            callbackMap.put(callbackId, callback);
            final String jsCode = String.format("window.JSSDK.getWebEditTextValue(%s)", callbackId);
            ((MainActivity) mContext).runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    ((MainActivity) mContext).mWebview.evaluateJavascript(jsCode, null);
                }
            });
        }

        void receiveMessage(int callbackId, String value) {
            if (callbackMap.containsKey(callbackId)) {
                callbackMap.get(callbackId).invoke(value);
            }
        }
    }

    //获取Web端输入的值并展示
    private void showNativeDialog2(String content) {
        new AlertDialog.Builder(this)
                .setTitle("获取Web端输入的值")
                .setMessage(content)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }


    class NativeBridge {
        private Context mContext;

        NativeBridge(Context context) {
            this.mContext = context;
        }

        //注意必须加这个注解
        @JavascriptInterface
        public void showNativeDialog(String content) {
            new AlertDialog.Builder(mContext)
                    .setTitle("Web端调用Native端")
                    .setMessage(content)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }

        @JavascriptInterface
        public void getNativeEdittextValue(int callbackId) {
            final MainActivity mainActivity = (MainActivity) mContext;
            String value = mainActivity.mEditText.getText().toString().trim();
            final String jsCode = String.format("window.JSSDK.receiveMessage('%s','%s')", callbackId, value);
            mainActivity.runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    mainActivity.mWebview.evaluateJavascript(jsCode, null);
                }
            });
        }

        @JavascriptInterface
        public void receiveMessage(int callbackId, String value) {
            ((MainActivity) mContext).nativeSDK.receiveMessage(callbackId, value);
        }
    }


    /***************************自定义URL Schema的方式*****************************/
    //自定义WebChromeClient拦截自定义的URL Schema
    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            if (!message.startsWith("jsbridge://")) {
                return super.onJsAlert(view, url, message, result);
            }
            String content = message.substring(message.indexOf("=") + 1);
            showNativeDialog(content);
            result.confirm();
            return true;
        }
    }

    //Web端调用原生端方法
    private void showNativeDialog(String content) {
        new AlertDialog.Builder(this)
                .setTitle("Web端调用Native端")
                .setMessage(content)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    //Native端调用Web端方法，只有一种方式，直接执行js代码
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showWebDialog(String content) {
        String jsCode = String.format("window.showWebDialog('%s')", content);
        mWebview.evaluateJavascript(jsCode, null);
    }
}
