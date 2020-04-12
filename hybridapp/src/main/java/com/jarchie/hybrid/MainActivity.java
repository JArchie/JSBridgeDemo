package com.jarchie.hybrid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;

/**
 * 作者: 乔布奇
 * 日期: 2020-04-12 23:10
 * 邮箱: jarchie520@gmail.com
 * 描述: 混合开发案例工程，使用DSBridge实现
 */
public class MainActivity extends AppCompatActivity {
    private DWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        webView.addJavascriptObject(new JSAPI(), null);
        webView.loadUrl("http://192.168.0.102:8080/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        changeTheme(0xFF7BECE0);
        return true;
    }

    //换肤方法
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeTheme(int color){
        //状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(color);
        //标题栏
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        //导航栏
        getWindow().setNavigationBarColor(color);
        //网页背景
        webView.callHandler("changeTheme",new Object[]{color});
    }

    class JSAPI {
        @JavascriptInterface
        public void nativeRequest(Object params, CompletionHandler<String> handler) {
            try {
                String url = ((JSONObject)params).getString("url");
                String data = request(url);
                handler.complete(data);
            } catch (Exception e) {
                handler.complete(e.getMessage());
                e.printStackTrace();
            }
        }

        private String request(String url) throws Exception{
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer result = new StringBuffer();
            String line;
            while ((line = reader.readLine())!=null){
                result.append(line);
            }
            return result.toString();
        }
    }
}
