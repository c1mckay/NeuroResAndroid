package edu.ucsd.neurores.activites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import edu.ucsd.neurores.R;
import edu.ucsd.neurores.network.RequestWrapper;

public class WebViewLoginActivity extends AppCompatActivity {
    WebView webView;
    String loginURL = "https://neurores.ucsd.edu/token/tokenGenerator.php";
    String tokenURL = "https://neurores.ucsd.edu/key/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_login);

        setupWebView();
        webView.loadUrl(loginURL);
    }

    private void setupWebView() {
        webView = (WebView) findViewById(R.id.login_webview);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                hideWaitMessage();
                if(url.startsWith(tokenURL)){
                    String token = extractToken(url);
                    saveTokenToPreferences(token);
                    RequestWrapper.registerFirebaseToken(getApplicationContext(), token, null);

                    startMainActivity();
                    finish();
                }

            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearCache(true);
        webView.clearHistory();
        clearCookies(this);
    }

    private String extractToken(String url){
        return url.substring(url.indexOf(tokenURL) + tokenURL.length());
    }

    private void saveTokenToPreferences(String loginToken){
        SharedPreferences sp = WebViewLoginActivity.this.getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(LoginActivity.TOKEN, loginToken);
        editor.commit();
    }

    private void startMainActivity(){
        Intent startApp = new Intent(WebViewLoginActivity.this, MainActivity.class);
        WebViewLoginActivity.this.startActivity(startApp);
    }

    private void hideWaitMessage(){
        findViewById(R.id.wait_message_text_view).setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
