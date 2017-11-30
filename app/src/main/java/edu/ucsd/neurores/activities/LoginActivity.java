package edu.ucsd.neurores.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.ucsd.neurores.network.HTTPRequestCompleteListener;
import edu.ucsd.neurores.R;
import edu.ucsd.neurores.network.RequestWrapper;

@SuppressLint("CommitPrefEdits")

public class LoginActivity extends AppCompatActivity{

    public static final String PREFS = "login_prefs";
    public static final String TOKEN = "neuro_res_token";
    public static final String NAME = "neuro_res_name";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mEmailSignInButton;

    private boolean loginInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginInProgress = false;


        setContentView(R.layout.activity_login);

        if( !isConnectedToNetwork()){
            showNoInternetConnectionToast();
        }


        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);


        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithWebView();
                //attemptLogin();
            }
        });

        removeToken();
        showToast(getString(R.string.hippa_statement), this);
    }

    private void loginWithWebView() {
        Intent startLogin = new Intent( LoginActivity.this, WebViewLoginActivity.class);
        LoginActivity.this.startActivity(startLogin);
    }

    private void showNoInternetConnectionToast() {
        showToast(getResources().getString(R.string.no_internet_connection_login), this);
    }

    private void showToast(final String message, final Context context){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message , Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isConnectedToNetwork() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void checkServerIsOnline(HTTPRequestCompleteListener onCompleteListener){
        RequestWrapper.checkServerIsOnline(this,  onCompleteListener);
    }

    private void removeToken(){
        SharedPreferences sp = LoginActivity.this.getSharedPreferences(LoginActivity.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(LoginActivity.TOKEN);
        //editor.remove(LoginActivity.NAME);
        editor.commit();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        final Context context = this;
        if(! isConnectedToNetwork()){
            showNoInternetConnectionToast();
            return;
        }

        checkServerIsOnline(new HTTPRequestCompleteListener() {

            @Override
            public void onComplete(String s) {
                loginWithWebView();
            }

            @Override
            public void onError(int i) {
                showToast(getString(R.string.cannot_connect_to_server), context);
            }
        });

    }

}

