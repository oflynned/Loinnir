package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.Message;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class ConversationFrag extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversation_frag, container, false);
        MessagesList messagesList = (MessagesList) view.findViewById(R.id.messagesList);
        MessagesListAdapter<Message> adapter = new MessagesListAdapter<>(LocalStorage.getID(getActivity()), loadImage());
        messagesList.setAdapter(adapter);

        JSONObject payload = new JSONObject();
        try {
            payload.put("fb_id", LocalStorage.getID(getActivity()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestClient.post(getActivity(), Endpoints.GET_NEARBY_COUNT, payload, new BaseJsonHttpResponseHandler<JSONObject>() {
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

        RestClient.post(getActivity(), Endpoints.GET_LOCALITY_MESSAGES, payload, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject userMessage = response.getJSONObject(i);
                        String message = userMessage.getString("message");
                        String sender = URLDecoder.decode(userMessage.getJSONObject("user").getString("name"), "UTF-8");
                        String profilePic = userMessage.getJSONObject("user").getString("profile_pic");
                        long timeSent = userMessage.getLong("time");

                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
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

        return view;
    }

    private ImageLoader loadImage() {
        return new ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, String url) {
                new GetImage(new NetworkCallback<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imageView.setImageBitmap(response);
                    }

                    @Override
                    public void onFailure() {

                    }
                }, url, true);
            }
        };
    }
}
