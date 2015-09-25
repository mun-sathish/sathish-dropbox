package in.sathish.dropbox.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;

import java.util.List;

import in.sathish.dropbox.R;
import in.sathish.dropbox.apidropbox.MainActivityApi;

public class LoginActivity extends AppCompatActivity {

    Button loginBtn, exitBtn;
    private DropboxAPI dropboxApi;
    private boolean isUserLoggedIn;

    private final static String DROPBOX_FILE_DIR = "";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "wpebbpyxdukopw2";
    private final static String ACCESS_SECRET = "dokw3nb04uxcrwa";
    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


//        String st = "/workspace/munna/";
//        String ts = st.replaceAll("\\\\w*(/.*/)?","");
//        Toast.makeText(this, st + "\n" + ts, Toast.LENGTH_LONG).show();


        loginBtn = (Button) findViewById(R.id.loginBtn);
        exitBtn = (Button) findViewById(R.id.exitBtn);

        Log.i("SATHISH", "OnCreate" );
        loggedIn(false);

        AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        AndroidAuthSession session;

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if(key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);

            session = new AndroidAuthSession(appKeyPair, token);
        } else {
            session = new AndroidAuthSession(appKeyPair);
        }

        dropboxApi = new DropboxAPI(session);

    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = (AndroidAuthSession)dropboxApi.getSession();
        if(session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                Log.i("SATHISH", "OnResume" );
                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
                loggedIn(true);



                Intent intent = new Intent(this, BrowseContentActivity.class);

                startActivity(intent);
                finish();
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox auth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loginBtn(View v)
    {
        if(isUserLoggedIn){
            dropboxApi.getSession().unlink();
            loggedIn(false);
        } else {
            ((AndroidAuthSession) dropboxApi.getSession())
//						.startOAuth2Authentication(MainActivityApi.this);
                    .startAuthentication(LoginActivity.this);
        }
    }
    public void exitBtn(View v) {
            finish();
    }

    public void loggedIn(boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
        Log.i("SATHISH", "loggedIn" );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
