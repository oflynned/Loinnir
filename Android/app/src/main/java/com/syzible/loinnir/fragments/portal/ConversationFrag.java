package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class ConversationFrag extends Fragment {
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversation_frag, container, false);
        textView = (TextView) view.findViewById(R.id.conversation_frag_text);

        JSONObject payload = new JSONObject();
        try {
            payload.put("fb_id", LocalStorage.getID(getActivity()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestClient.post(getActivity(), Endpoints.GET_LOCALITY_MESSAGES, payload, new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                String conversationInLocality = "";
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject userMessage = response.getJSONObject(i);
                        String message = userMessage.getString("message");
                        String sender = URLDecoder.decode(userMessage.getJSONObject("user").getString("name"), "UTF-8");
                        String profilePic = userMessage.getJSONObject("user").getString("profile_pic");
                        long timeSent = userMessage.getLong("time");

                        System.out.println(message);
                        conversationInLocality += message + " (" + sender + ")\n";
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                textView.setText(conversationInLocality);
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
}
