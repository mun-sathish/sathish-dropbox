package in.sathish.dropbox.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;

import in.sathish.dropbox.R;


/**
 * Created by Sathish Mun on 23/9/15.
 */

public class LoginActivity extends AppCompatActivity {

    Button loginBtn, exitBtn;
    private DropboxAPI dropboxApi;
    private boolean isUserLoggedIn;

    // Static Dropbox Api keys to connect
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "wpebbpyxdukopw2";
    private final static String ACCESS_SECRET = "dokw3nb04uxcrwa";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initLayout();    //set up layout elements
        loggedIn(false); //not logged in initally
        initDropbox();   // initialize the key authentication
    }

    private void initLayout() {
        loginBtn = (Button) findViewById(R.id.loginBtn);
        exitBtn = (Button) findViewById(R.id.exitBtn);
    }

    private void initDropbox() {
        AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        AndroidAuthSession session;
        // storing for future use
        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeyPair, token);
        } else {
            session = new AndroidAuthSession(appKeyPair);
        }

        dropboxApi = new DropboxAPI(session); //dropboxApi contains to access for all elements

    }

    // to check out the user loggedin or not and action performed accordingly
    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = (AndroidAuthSession) dropboxApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // concluding the authentication of api
                session.finishAuthentication();
                Log.i("SATHISH", "OnResume");
                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
                loggedIn(true); //access been granted, therefore it is allowed to log in

                // if the access is granted... then it next logged in activity been opened
                Intent intent = new Intent(this, BrowseContentActivity.class);
                startActivity(intent);
                finish(); //closing the current activity
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox auth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // on press the login button, it opens the dropbox for permission access of the user account
    public void loginBtn(View v) {
        if (isUserLoggedIn) {
            // already logged in... it is been logged out(for handling exception)
            dropboxApi.getSession().unlink();
            loggedIn(false);
        } else {
            // opens the dropbox session
            ((AndroidAuthSession) dropboxApi.getSession())
                  .startAuthentication(LoginActivity.this);
        }
    }

    // when exit button is clicked
    public void exitBtn(View v) {
        finish();
    }

    // sets the status of user logged in or not
    public void loggedIn(boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
        Log.i("SATHISH", "loggedIn");
    }

}
