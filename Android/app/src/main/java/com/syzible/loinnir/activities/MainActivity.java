package com.syzible.loinnir.activities;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.syzible.loinnir.R;
import com.syzible.loinnir.fragments.portal.ConversationsFrag;
import com.syzible.loinnir.fragments.portal.MapFrag;
import com.syzible.loinnir.fragments.portal.RouletteFrag;
import com.syzible.loinnir.network.PicassoSSL;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.BitmapUtils;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.EmojiUtils;
import com.syzible.loinnir.utils.FacebookUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // TODO consider using emojis in the snackbars to make it look more fun

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        setFragment(new MapFrag());

        String name = LocalStorage.getPref(LocalStorage.Pref.name, this);
        name = name.split(" ")[0];
        DisplayUtils.generateSnackbar(this,
                "Fáilte ar ais, a " + name + "! " + EmojiUtils.getEmoji(EmojiUtils.HAPPY_EMOJI));

        // set up nav bar header for personalisation
        View headerView = navigationView.getHeaderView(0);

        TextView userName = (TextView) headerView.findViewById(R.id.nav_header_name);
        userName.setText(LocalStorage.getPref(LocalStorage.Pref.name, this));

        TextView localityName = (TextView) headerView.findViewById(R.id.nav_header_locality);
        localityName.setText("");

        final ImageView profilePic = (ImageView) headerView.findViewById(R.id.nav_header_pic);
        String picUrl = LocalStorage.getPref(LocalStorage.Pref.profile_pic, this);

        Bitmap localProfilePic = BitmapFactory.decodeResource(getResources(), R.drawable.profile_pic);
        Bitmap croppedPic = BitmapUtils.getCroppedCircle(localProfilePic);
        profilePic.setImageBitmap(croppedPic);

        // TODO SSL request not working
        RestClient.getExternal(picUrl, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                Bitmap pic = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
                profilePic.setImageBitmap(BitmapUtils.getCroppedCircle(pic));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            // TODO make an alert dialog, could be annoying for the user otherwise
            this.finish();
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_around_me) {
            setFragment(new MapFrag());
        } else if (id == R.id.nav_conversations) {
            setFragment(new ConversationsFrag());
        } else if (id == R.id.nav_roulette) {
            setFragment(new RouletteFrag());
        } else if (id == R.id.nav_rate) {

        } else if (id == R.id.nav_log_out) {
            FacebookUtils.deleteToken(this);
            finish();
            startActivity(new Intent(this, AuthenticationActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.portal_frame, fragment)
                .addToBackStack(null)
                .commit();
    }
}
