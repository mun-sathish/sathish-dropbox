package in.sathish.dropbox.main;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import in.sathish.dropbox.main.BrowseContentAdapter.onBrowseAdapterListener;

public class BrowseContentActivity extends AppCompatActivity implements onBrowseAdapterListener {
    Toolbar toolbar;
    TextView filePath;
    private Dialog dialog = null;
    private UltimateRecyclerView recyclerView;
    private BrowseContentAdapter adapter;
    private List<BrowseContentDTO> allRowItemList = new ArrayList<>();
    private List<String> pathStack = new ArrayList<>();
    private List<Integer> postionStack = new ArrayList<>();
    int pathStackIndex = -1;
    int recyclerPos = 0;
    private DropboxAPI dropboxApi;
    Boolean topState;
    private final static String RIGHT_ARROW = "> ";
    private final static String ROOT_PATH = "Dropbox ";
    private final static String SLASH = "/";
    private static String DROPBOX_FILE_DIR = "/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "wpebbpyxdukopw2";
    private final static String ACCESS_SECRET = "dokw3nb04uxcrwa";
    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_content);

        //set up toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Dropbox");

        filePath = (TextView) findViewById(R.id.filePath);
        filePath.setText(ROOT_PATH + RIGHT_ARROW);

        AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        AndroidAuthSession session;
        AccessTokenPair token = new AccessTokenPair(ACCESS_KEY, ACCESS_SECRET);
        session = new AndroidAuthSession(appKeyPair, token);
        dropboxApi = new DropboxAPI(session);

        session = (AndroidAuthSession)dropboxApi.getSession();
        if(session.authenticationSuccessful()) {
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

        initRecyclerView();
        logout_layout();
        dialog.hide();
        ProgressDialogUtil.showDialog(this, "Loading...");
        setRecyclerView(true);
        populateFiles("front");
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

        if(pathStackIndex != 0)
        {
            ProgressDialogUtil.showDialog(this, "Loading...");
            pathStack.remove(pathStackIndex);
            pathStackIndex--;
            DROPBOX_FILE_DIR = pathStack.get(pathStackIndex);
            setRecyclerView(false);
            populateFiles("back");

        }
        else { finish(); }
    }

    private void setRecyclerView(boolean top)  { topState = top; }

    private void populateFiles(String direction) {
        if(Objects.equals(direction, "front")) {
            pathStack.add(DROPBOX_FILE_DIR);
            postionStack.add(recyclerPos);
            pathStackIndex++;
        }

        filePath.setText(ROOT_PATH + RIGHT_ARROW + DROPBOX_FILE_DIR);
        allRowItemList.clear();
        ListFilesTask listFilesTask = new ListFilesTask();
        listFilesTask.execute();

   /*     try {
            DropboxAPI.Entry directory = dropboxApi.metadata(DROPBOX_FILE_DIR, 1000, null, true, null);
            for(DropboxAPI.Entry entry : directory.contents) {

                BrowseContentDTO details = new BrowseContentDTO();

                details.setFileName(entry.fileName());
                details.setFileSize(entry.size);
                details.setFileModified(entry.modified);
                details.setIsFolder((entry.icon == "folder"));
                details.setFilePath(entry.path);

                allRowItemList.add(details);
                adapter.notifyDataSetChanged();
                ProgressDialogUtil.hidePDialog();
            }
        } catch (DropboxException e) {
        }
  */  }

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
            Intent i=new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sathish Dropbox");
            i.putExtra(android.content.Intent.EXTRA_TEXT, "Hey!!! Check out the quick & faster Dropbox\n\nhttp://www.sathishmun.comule.com/");
            startActivity(Intent.createChooser(i,"Share via"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickFolder(String pathName, int position) {
        DROPBOX_FILE_DIR = pathName + SLASH;
        recyclerPos = position;
        ProgressDialogUtil.showDialog(this,"Loading...");
        setRecyclerView(true);
        populateFiles("front");
    }



    public class ListFilesTask extends AsyncTask<Void, Void, List<BrowseContentDTO>> {

        @Override
        protected List<BrowseContentDTO> doInBackground(Void... params) {


            try {

                DropboxAPI.Entry directory = dropboxApi.metadata(DROPBOX_FILE_DIR, 1000, null, true, null);
                for(DropboxAPI.Entry entry : directory.contents) {

                    BrowseContentDTO details = new BrowseContentDTO();

                    details.setFileName(entry.fileName());
                    if(!Objects.equals(entry.icon, "folder")){ details.setFileSize(entry.size); }
                    details.setFileModified(entry.modified);
//                    details.setIsFolder((entry.icon == "folder"));
                    details.setFilePath(entry.path);

                    if(Objects.equals(entry.icon, "folder"))
                        details.setIsFolder(true);
                    else
                        details.setIsFolder(false);

                    if(Objects.equals(entry.icon, "page_white_picture")){ details.setFileImage(R.mipmap.image); }
                    else if(Objects.equals(entry.icon, "page_white_code")){ details.setFileImage(R.mipmap.code); }
                    else if(Objects.equals(entry.icon, "page_white_sound")){ details.setFileImage(R.mipmap.music); }
                    else if(Objects.equals(entry.icon, "page_white_film")){ details.setFileImage(R.mipmap.video); }
                    else if(Objects.equals(entry.icon, "page_white_text")){ details.setFileImage(R.mipmap.text); }
                    else if(Objects.equals(entry.icon, "folder")){ details.setFileImage(R.mipmap.folder); }
                    else if(Objects.equals(entry.icon, "page_white_acrobat")){ details.setFileImage(R.mipmap.pdf); }
                    else if(Objects.equals(entry.icon, "page_white_word")){ details.setFileImage(R.mipmap.word); }
                    else if(Objects.equals(entry.icon, "page_white_powerpoint")){ details.setFileImage(R.mipmap.ppt); }
                    else { details.setFileImage(R.mipmap.unknown); }


                    allRowItemList.add(details);


          //
                }
            } catch (DropboxException e) {
            }
            return allRowItemList;
        }
        @Override
        protected void onPostExecute(List<BrowseContentDTO> result) {
//            allRowItemList.add(result);
            adapter.notifyDataSetChanged();
            if(topState) { recyclerView.scrollVerticallyToPosition(0); }
            else {
                recyclerView.scrollVerticallyToPosition(postionStack.get(pathStackIndex+1));
                postionStack.remove(pathStackIndex+1);
            }

            ProgressDialogUtil.hidePDialog();
        }

    }


    public void logout(View v) { dialog.show(); }

    public void logout_layout(){
        //itemSizes = itemDetailsDTO.getSizes();

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


    Button yesBtn, noBtn;
    private void initDialogView() {

        dialog.setCanceledOnTouchOutside(true);

        yesBtn = (Button) dialog.findViewById(R.id.yesBtn);
        noBtn = (Button) dialog.findViewById(R.id.noBtn);

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropboxApi.getSession().unlink();
                finish();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}
