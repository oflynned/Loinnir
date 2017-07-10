package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.Message;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.services.CachingUtil;
import com.syzible.loinnir.utils.BitmapUtils;
import com.syzible.loinnir.utils.BroadcastFilters;
import com.syzible.loinnir.utils.EncodingUtils;
import com.syzible.loinnir.utils.JSONUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class LocalityConversationFrag extends Fragment {

    private Date lastLoadedDate;
    private int loadedCount;
    private String lastLoadedMessageId;

    private MessagesListAdapter<Message> adapter;
    private BroadcastReceiver newLocalityInformationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Invoked Locality Receiver!");
            if (intent.getAction().equals(BroadcastFilters.new_locality_info_update.toString())) {
                // TODO change this, the screen refreshes completely D:
                // clear the messages and reload
                loadMessages();
            }
        }
    };

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.conversation_frag, container, false);

        setupAdapter(view);
        loadMessages();

        return view;
    }

    @Override
    public void onResume() {
        setupAdapter(view);
        loadMessages();
        getActivity().registerReceiver(newLocalityInformationReceiver,
                new IntentFilter(BroadcastFilters.new_locality_info_update.toString()));

        super.onResume();

    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(newLocalityInformationReceiver);
        super.onPause();
    }

    private void setupAdapter(View view) {
        MessagesListAdapter.HoldersConfig holdersConfig = new MessagesListAdapter.HoldersConfig();
        holdersConfig.setIncoming(IncomingMessage.class, R.layout.chat_message_layout);

        adapter = new MessagesListAdapter<>(LocalStorage.getID(getActivity()), holdersConfig, loadImage());
        MessagesList messagesList = (MessagesList) view.findViewById(R.id.messages_list);
        messagesList.setAdapter(adapter);

        MessageInput messageInput = (MessageInput) view.findViewById(R.id.message_input);
        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(final CharSequence input) {
                RestClient.post(getActivity(), Endpoints.GET_USER, JSONUtils.getIdPayload(getActivity()),
                        new BaseJsonHttpResponseHandler<JSONObject>() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                                try {
                                    final User me = new User(response);
                                    String messageContent = input.toString();

                                    Message message = new Message(LocalStorage.getID(getActivity()), me, System.currentTimeMillis(), messageContent);
                                    adapter.addToStart(message, true);

                                    // send to server
                                    JSONObject messagePayload = new JSONObject();
                                    messagePayload.put("fb_id", LocalStorage.getID(getActivity()));
                                    messagePayload.put("message", EncodingUtils.encodeText(message.getText()));

                                    RestClient.post(getActivity(), Endpoints.SEND_LOCALITY_MESSAGE, messagePayload, new BaseJsonHttpResponseHandler<JSONObject>() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                                            System.out.println("Message submitted to locality (" + me.getLocality() + ")");
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {

                                        }

                                        @Override
                                        protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                                            System.out.println(rawJsonData);
                                            return new JSONObject(rawJsonData);
                                        }
                                    });
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
        });


        RestClient.post(getActivity(), Endpoints.GET_NEARBY_COUNT, JSONUtils.getIdPayload(getActivity()),
                new BaseJsonHttpResponseHandler<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                        try {
                            String localityName = response.getString("locality");
                            int nearbyUsers = response.getInt("count") + 1;
                            String title = localityName + " (" + nearbyUsers + " anseo)";
                            getActivity().setTitle(title);
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

    private void loadMessages() {
        setupAdapter(view);

        RestClient.post(getActivity(), Endpoints.GET_LOCALITY_MESSAGES, JSONUtils.getIdPayload(getActivity()),
                new BaseJsonHttpResponseHandler<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                        ArrayList<Message> messages = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject userMessage = response.getJSONObject(i);
                                String id = userMessage.getString("_id");
                                String messageContent = userMessage.getString("message");
                                long timeSent = userMessage.getLong("time");

                                User user = new User(userMessage.getJSONObject("user"));
                                Message message = new Message(id, user, timeSent, messageContent);
                                messages.add(message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.addToEnd(messages, true);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                        System.out.println("failed?");
                    }

                    @Override
                    protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                        return new JSONArray(rawJsonData);
                    }
                });
    }

    private ImageLoader loadImage() {
        return new ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, final String url) {
                // can only use Facebook to sign up so use the embedded id in the url
                final String fileName = url.split("/")[3];

                if (!CachingUtil.doesImageExist(getActivity(), fileName)) {
                    System.out.println("Does not exist!");
                    new GetImage(new NetworkCallback<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            Bitmap croppedImage = BitmapUtils.getCroppedCircle(response);
                            CachingUtil.cacheImage(getActivity(), fileName, croppedImage);
                            imageView.setImageBitmap(croppedImage);
                        }

                        @Override
                        public void onFailure() {
                            System.out.println("dl failure on chat pic");
                        }
                    }, url, true).execute();
                } else {
                    System.out.println("Exists!");
                    Bitmap cachedImage = CachingUtil.getCachedImage(getActivity(), fileName);
                    imageView.setImageBitmap(cachedImage);
                }
            }
        };
    }
}
