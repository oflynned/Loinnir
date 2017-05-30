package com.syzible.loinnir.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.fragments.portal.LocalityConversationFrag;
import com.syzible.loinnir.fragments.portal.ConversationsListFrag;
import com.syzible.loinnir.fragments.portal.MapFrag;
import com.syzible.loinnir.fragments.portal.PartnerConversationFrag;
import com.syzible.loinnir.fragments.portal.RouletteFrag;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.services.AlarmReceiver;
import com.syzible.loinnir.services.LocationService;
import com.syzible.loinnir.utils.BitmapUtils;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.EmojiUtils;
import com.syzible.loinnir.utils.JSONUtils;
import com.syzible.loinnir.utils.LanguageUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private View headerView;
    private AlarmReceiver alarmReceiver = new AlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        alarmReceiver.cancelAlarm(this);
        startService(new Intent(this, LocationService.class));
        registerBroadcastReceiver();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocality();
            }
        });
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        headerView = navigationView.getHeaderView(0);

        greetUser();
        setUpDrawer();
        checkNotificationInvocation();
    }

    private void registerBroadcastReceiver() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("finish_main_activity")) {
                    finish();
                }
            }
        }, new IntentFilter("finish_main_activity"));
    }

    private void greetUser() {
        String name = LocalStorage.getStringPref(LocalStorage.Pref.name, this);
        name = name.split(" ")[0];
        DisplayUtils.generateSnackbar(this, "Fáilte romhat, a " + LanguageUtils.getVocative(name) + "! " +
                EmojiUtils.getEmoji(EmojiUtils.HAPPY));
    }

    private void checkNotificationInvocation() {
        String invocationType = getIntent().getStringExtra("invoker");
        if (invocationType != null) {
            switch (invocationType) {
                case "notification":
                    String partnerId = getIntent().getStringExtra("user");
                    JSONObject chatPayload = new JSONObject();
                    try {
                        chatPayload.put("fb_id", partnerId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // get partner details and then open the chat fragment
                    RestClient.post(this, Endpoints.GET_USER, chatPayload, new BaseJsonHttpResponseHandler<JSONObject>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                            try {
                                User partner = new User(response);
                                PartnerConversationFrag frag = new PartnerConversationFrag()
                                        .setPartner(partner);
                                MainActivity.clearBackstack(getFragmentManager());
                                MainActivity.setFragment(getFragmentManager(), frag);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {

                        }

                        @Override
                        protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                            return new JSONObject(rawJsonData);
                        }
                    });
            }
        } else {
            setFragment(getFragmentManager(), new MapFrag());
        }
    }

    private void setLocality() {
        RestClient.post(getApplicationContext(), Endpoints.GET_USER, JSONUtils.getIdPayload(this), new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                try {
                    TextView localityName = (TextView) headerView.findViewById(R.id.nav_header_locality);
                    String locality = response.getString("locality");
                    localityName.setText(locality);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {

            }

            @Override
            protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return new JSONObject(rawJsonData);
            }
        });
    }

    private void setUpDrawer() {
        TextView userName = (TextView) headerView.findViewById(R.id.nav_header_name);
        userName.setText(LocalStorage.getStringPref(LocalStorage.Pref.name, this));

        setLocality();

        final ImageView profilePic = (ImageView) headerView.findViewById(R.id.nav_header_pic);
        String picUrl = LocalStorage.getStringPref(LocalStorage.Pref.profile_pic, this);

        new GetImage(new NetworkCallback<Bitmap>() {
            @Override
            public void onResponse(Bitmap pic) {
                Bitmap croppedPic = BitmapUtils.getCroppedCircle(pic);
                profilePic.setImageBitmap(croppedPic);
            }

            @Override
            public void onFailure() {

            }
        }, picUrl, true).execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // if there's only one fragment on the stack we should prevent the default
            // popping to ask for the user's permission to close the app
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("An Aip a Dhúnadh?")
                        .setMessage("Má bhrúitear an chnaipe \"Dún\", dúnfar an aip. An bhfuil tú cinnte go bhfuil sé seo ag teastáil uait a dhéanamh?")
                        .setPositiveButton("Dún", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Ná dún", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            } else {
                super.onBackPressed();
            }
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
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_around_me) {
            clearBackstack(getFragmentManager());
            setFragment(getFragmentManager(), new MapFrag());
        } else if (id == R.id.nav_conversations) {
            clearBackstack(getFragmentManager());

            RestClient.post(this, Endpoints.GET_PAST_CONVERSATION_PREVIEWS,
                    JSONUtils.getIdPayload(this),
                    new BaseJsonHttpResponseHandler<JSONArray>() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                            setFragment(getFragmentManager(), new ConversationsListFrag().setResponse(response));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {

                        }

                        @Override
                        protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                            return new JSONArray(rawJsonData);
                        }
                    }
            );

        } else if (id == R.id.nav_roulette) {
            clearBackstack(getFragmentManager());
            setFragment(getFragmentManager(), new RouletteFrag());
        } else if (id == R.id.nav_nearby) {
            clearBackstack(getFragmentManager());
            setFragment(getFragmentManager(), new LocalityConversationFrag());
        } else if (id == R.id.nav_rate) {
            Intent intent;

            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            } catch (android.content.ActivityNotFoundException e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
            }
            startActivity(intent);

        } else if (id == R.id.nav_more_apps) {
            Intent intent;

            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Syzible"));
            } catch (android.content.ActivityNotFoundException e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/search?q:Syzible"));
            }

            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        RestClient.post(getApplicationContext(), Endpoints.GET_USER, JSONUtils.getIdPayload(this),
                new BaseJsonHttpResponseHandler<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                        try {
                            TextView localityName = (TextView) headerView.findViewById(R.id.nav_header_locality);
                            String locality = response.getString("locality");
                            localityName.setText(locality);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {

                    }

                    @Override
                    protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                        return new JSONObject(rawJsonData);
                    }
                });

        return true;
    }

    public static void setFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.portal_frame, fragment)
                .commit();
    }

    public static void setFragmentBackstack(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.portal_frame, fragment)
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    public static void removeFragment(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();
    }

    public static void clearBackstack(FragmentManager fragmentManager) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
