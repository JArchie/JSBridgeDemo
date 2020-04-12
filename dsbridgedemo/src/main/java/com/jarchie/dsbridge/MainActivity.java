package com.jarchie.dsbridge;

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

import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;

/**
 * 作者: 乔布奇
 * 日期: 2020-04-12 17:50
 * 邮箱: jarchie520@gmail.com
 * 描述: Native端页面展示层，上方为Web页面，下方为Android页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DWebView mWebview;
    private EditText mEditText;
    private TextView mShowBtn2, mRefreshWeb;

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
        mWebview.setWebChromeClient(new WebChromeClient());
        mWebview.addJavascriptObject(new JSAPI(this), null);
    }

    //初始化事件监听
    private void initListener() {
        mShowBtn2.setOnClickListener(this);
        mRefreshWeb.setOnClickListener(this);
    }

    //初始化绑定控件
    private void initView() {
        mWebview = findViewById(R.id.mWebview);
        mEditText = findViewById(R.id.mEditText);
        mShowBtn2 = findViewById(R.id.mShowBtn2);
        mRefreshWeb = findViewById(R.id.mRefreshWeb);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mShowBtn2:
                mWebview.callHandler("getWebEditTextValue", null, new OnReturnValue<String>() {
                    @Override
                    public void onValue(String retValue) {
                        showNativeDialog("Web端输入值：" + retValue);
                    }
                });
                break;
            case R.id.mRefreshWeb:
                mWebview.loadUrl("http://192.168.0.102:8080/?timestamp" + new Date().getTime());
                break;
        }
    }

    class JSAPI {
        private Context mContext;

        public JSAPI(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void getNativeEditTextValue(Object msg, CompletionHandler<String> handler) {
            String content = ((MainActivity) mContext).mEditText.getText().toString().trim();
            handler.complete(content);
        }
    }

    //获取Web端输入的值并展示
    private void showNativeDialog(String content) {
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

}
