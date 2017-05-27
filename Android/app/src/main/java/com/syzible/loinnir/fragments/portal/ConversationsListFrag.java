package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.Conversation;
import com.syzible.loinnir.objects.Message;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.utils.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class ConversationsListFrag extends Fragment {

    private ArrayList<Conversation> conversations = new ArrayList<>();
    private DialogsListAdapter<Conversation> dialogsListAdapter;
    private DialogsList dialogsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversations_list_frag, container, false);

        dialogsList = (DialogsList) view.findViewById(R.id.conversations_list);
        dialogsListAdapter = new DialogsListAdapter<>(loadImage());

        getActivity().setTitle(getResources().getString(R.string.app_name));

        RestClient.post(getActivity(), Endpoints.GET_PAST_CONVERSATION_PREVIEWS,
                JSONUtils.getIdPayload(getActivity()),
                new BaseJsonHttpResponseHandler<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                        for (int i=0; i<response.length(); i++) {
                            try {
                                JSONObject o = response.getJSONObject(i);
                                User sender = new User(o.getJSONObject("user"));
                                Message message = new Message(sender, o.getJSONObject("message"));
                                Conversation conversation = new Conversation(sender, message);

                                conversations.add(conversation);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        dialogsListAdapter.addItems(conversations);
                        dialogsList.setAdapter(dialogsListAdapter);
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
        return view;
    }

    private void onNewMessage(String dialogId, Message message) {
        if (!dialogsListAdapter.updateDialogWithMessage(dialogId, message)) {
            // doesn't already exist, reload entire list or create a new dialog
        }

        // else update, check if read, AND reorder
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private ImageLoader loadImage() {
        return new ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, final String url) {
                new GetImage(new NetworkCallback<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imageView.setImageBitmap(response);
                    }

                    @Override
                    public void onFailure() {
                        System.out.println("dl failure on chat pic");
                    }
                }, url, true);
            }
        };
    }
}
