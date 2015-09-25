package in.sathish.dropbox.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.sathish.dropbox.R;
import in.sathish.dropbox.adapter.BrowseContentAdapter;
import in.sathish.dropbox.adapter.BrowseContentAdapter.onBrowseAdapterListener;
import in.sathish.dropbox.dto.BrowseContentDTO;
import in.sathish.dropbox.util.ProgressDialogUtil;


/**
 * Created by Sathish Mun on 23/9/15.
 */

public class BrowseContentActivity extends AppCompatActivity implements onBrowseAdapterListener {

    private DropboxAPI dropboxApi;   //controls all the contents of the api
    Toolbar toolbar;
    Button yesBtn, noBtn;
    TextView filePath;              //to track the current path in the dropbox
    private Dialog dialog = null;
    private UltimateRecyclerView recyclerView;
    private BrowseContentAdapter adapter;
    private List<BrowseContentDTO> allRowItemList = new ArrayList<>();  //stores contents of all the elements in the dropbox
    private List<String> pathStack = new ArrayList<>();          //stores all the travelled path in the form of stack
    private List<Integer> postionStack = new ArrayList<>();      //stores the position at which the element is clicked
    int pathStackIndex = -1;                                    //pointer to take care of the path & position stack
    int recyclerPos = 0;                                        // to set the position of the view
    Boolean topState;                                           //check whether the path is a newly added one..

    // all static string values
    private final static String RIGHT_ARROW = "> ";
    private final static String ROOT_PATH = "Dropbox ";
    private final static String SLASH = "/";
    private static String DROPBOX_FILE_DIR = "/";           // to change the directory of the one which is been clicked
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "wpebbpyxdukopw2";
    private final static String ACCESS_SECRET = "dokw3nb04uxcrwa";
    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_content);
        initDropbox();      //getting access to the user account
        initLayout();       //initialze the toolbar and elements
        initRecyclerView(); //initialize the recycler view
        logout_layout();    // initialize the logout button layout
        dialog.hide();      //intially it is been since it is not clicked
        ProgressDialogUtil.showDialog(this, "Loading...");
        setRecyclerView(true);    //to set the topState value
        populateFiles("front");   // to populate the content in the recyclerView
    }


    private void initDropbox()
    {
        AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        AndroidAuthSession session;
        AccessTokenPair token = new AccessTokenPair(ACCESS_KEY, ACCESS_SECRET);
        session = new AndroidAuthSession(appKeyPair, token);
        dropboxApi = new DropboxAPI(session);

        session = (AndroidAuthSession) dropboxApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox auth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initLayout()
        {//set up toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.toolbar_icon);
        filePath = (TextView) findViewById(R.id.filePath);
        filePath.setText(ROOT_PATH + RIGHT_ARROW);
    }

    private void initRecyclerView() {
        recyclerView = (UltimateRecyclerView) findViewById(R.id.listView_contents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        adapter = new BrowseContentAdapter(this, allRowItemList, this);
        //set adapter
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (pathStackIndex != 0) {
            ProgressDialogUtil.showDialog(this, "Loading...");
            pathStack.remove(pathStackIndex);
            pathStackIndex--;
            DROPBOX_FILE_DIR = pathStack.get(pathStackIndex);
            setRecyclerView(false);
            populateFiles("back");

        } else {
            finish();
        }
    }

    private void setRecyclerView(boolean top) {
        topState = top;
    }

    private void populateFiles(String direction) {
        if (Objects.equals(direction, "front")) {    //check whether it is clicked on back pressed
            pathStack.add(DROPBOX_FILE_DIR);
            postionStack.add(recyclerPos);
            pathStackIndex++;
        }
        filePath.setText(ROOT_PATH + RIGHT_ARROW + DROPBOX_FILE_DIR);  //dynamically setting up the current path
        allRowItemList.clear();                         //on each populate, contents are differed, so it is cleared
        ListFilesTask listFilesTask = new ListFilesTask();  //initialzing the AsyncTask
        listFilesTask.execute();         //executing the asyncTask to set the recyclerView Contents
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {
            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sathish Dropbox");
            i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey!!! Check out the quick & faster Dropbox\n\nhttp://www.sathishmun.comule.com/");
            startActivity(Intent.createChooser(i, "Share via"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickFolder(String pathName, int position) {
        DROPBOX_FILE_DIR = pathName + SLASH;
        recyclerPos = position;
        ProgressDialogUtil.showDialog(this, "Loading...");
        setRecyclerView(true);
        populateFiles("front");
    }


    public class ListFilesTask extends AsyncTask<Void, Void, List<BrowseContentDTO>> {
        @Override
        protected List<BrowseContentDTO> doInBackground(Void... params) {
            try {
                DropboxAPI.Entry directory = dropboxApi.metadata(DROPBOX_FILE_DIR, 1000, null, true, null);
                for (DropboxAPI.Entry entry : directory.contents) {
                    BrowseContentDTO details = new BrowseContentDTO();

                    //set filename
                    details.setFileName(entry.fileName());

                    //set filesize if not a folder
                    if (!Objects.equals(entry.icon, "folder")) {
                        details.setFileSize(entry.size);
                    }

                    //set modified time and date
                    details.setFileModified(entry.modified);

                    //set current file path
                    details.setFilePath(entry.path);

                    //set whether it is a folder or not
                    if (Objects.equals(entry.icon, "folder"))  details.setIsFolder(true);
                    else details.setIsFolder(false);

                    // sets the resource accordingly to the format of the file
                    if (Objects.equals(entry.icon, "page_white_picture")) {
                        details.setFileImage(R.mipmap.image);
                    } else if (Objects.equals(entry.icon, "page_white_code")) {
                        details.setFileImage(R.mipmap.code);
                    } else if (Objects.equals(entry.icon, "page_white_sound")) {
                        details.setFileImage(R.mipmap.music);
                    } else if (Objects.equals(entry.icon, "page_white_film")) {
                        details.setFileImage(R.mipmap.video);
                    } else if (Objects.equals(entry.icon, "page_white_text")) {
                        details.setFileImage(R.mipmap.text);
                    } else if (Objects.equals(entry.icon, "folder")) {
                        details.setFileImage(R.mipmap.folder);
                    } else if (Objects.equals(entry.icon, "page_white_acrobat")) {
                        details.setFileImage(R.mipmap.pdf);
                    } else if (Objects.equals(entry.icon, "page_white_word")) {
                        details.setFileImage(R.mipmap.word);
                    } else if (Objects.equals(entry.icon, "page_white_powerpoint")) {
                        details.setFileImage(R.mipmap.ppt);
                    } else {
                        details.setFileImage(R.mipmap.unknown);
                    }

                    //adding all details of a single content to the list
                    allRowItemList.add(details);
                }
            } catch (DropboxException e) { }
            return allRowItemList;
        }

        //after the background process is over
        @Override
        protected void onPostExecute(List<BrowseContentDTO> result) {
            adapter.notifyDataSetChanged();
            if (topState) {                                //if it is clicked.. then set position to 0
                recyclerView.scrollVerticallyToPosition(0);
            } else {                                       //if is back pressed.. set position accordingly which is stored in positionStack
                recyclerView.scrollVerticallyToPosition(postionStack.get(pathStackIndex + 1));
                postionStack.remove(pathStackIndex + 1);
            }
            ProgressDialogUtil.hidePDialog();              //hide the dialog
        }

    }

    //when logout button is clicked
    public void logout(View v) { dialog.show(); }

    //setting up the logout layout
    public void logout_layout() {
        if (dialog == null) {
            dialog = new Dialog(this, R.style.DialogSlideAnim);
        }
        dialog.setContentView(R.layout.logout);
        initDialogView();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.show();
        lp.dimAmount = 0.7f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    private void initDialogView() {
        dialog.setCanceledOnTouchOutside(true);
        yesBtn = (Button) dialog.findViewById(R.id.yesBtn);
        noBtn = (Button) dialog.findViewById(R.id.noBtn);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   //when 'yes' button is clicked
                DROPBOX_FILE_DIR = "/";
                dropboxApi.getSession().unlink();
                finish();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        //when 'no' button is clicked
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}
