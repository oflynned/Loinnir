package com.syzible.loinnir.fragments.portal;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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
import com.syzible.loinnir.activities.MainActivity;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.Conversation;
import com.syzible.loinnir.objects.Message;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.utils.BitmapUtils;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.JSONUtils;
import com.syzible.loinnir.utils.LanguageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class ConversationsListFrag extends Fragment implements
        DialogsListAdapter.OnDialogClickListener<Conversation>,
        DialogsListAdapter.OnDialogLongClickListener<Conversation> {

    private ArrayList<Conversation> conversations = new ArrayList<>();
    private DialogsListAdapter<Conversation> dialogsListAdapter;
    private DialogsList dialogsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversations_list_frag, container, false);

        conversations.clear();
        dialogsList = (DialogsList) view.findViewById(R.id.conversations_list);
        dialogsListAdapter = new DialogsListAdapter<>(loadImage());

        getActivity().setTitle(getResources().getString(R.string.app_name));

        RestClient.post(getActivity(), Endpoints.GET_PAST_CONVERSATION_PREVIEWS,
                JSONUtils.getIdPayload(getActivity()),
                new BaseJsonHttpResponseHandler<JSONArray>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
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
                        dialogsListAdapter.setOnDialogClickListener(ConversationsListFrag.this);
                        dialogsListAdapter.setOnDialogLongClickListener(ConversationsListFrag.this);
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

    @Override
    public void onDialogClick(Conversation conversation) {
        User partner = (User) conversation.getUsers().get(0);
        PartnerConversationFrag frag = new PartnerConversationFrag().setPartner(partner);
        MainActivity.setFragmentBackstack(getFragmentManager(), frag);
    }

    @Override
    public void onDialogLongClick(final Conversation conversation) {
        final String blockee = conversation.getDialogName().split(" ")[0];

        new AlertDialog.Builder(getActivity())
                .setTitle("Cosc a Chur ar " + LanguageUtils.lenite(conversation.getDialogName()) + "?")
                .setMessage("Má chuireann tú cosc ar úsáideoir araile, ní féidir leat nó le " + blockee + " dul i dteagmháil lena chéile. " +
                        "Bain úsáid as seo amháin go bhfuil tú cinnte nach dteastaíonn uait faic a chloisteáil a thuilleadh ón úsáideoir seo. " +
                        "Cur cosc ar dhuine má imrítear bulaíocht ort, nó mura dteastaíonn tuilleadh teagmhála uait. " +
                        "Má athraíonn tú do mheabhair, téigh chuig socruithe agus bainistigh cé atá faoi chosc.")
                .setPositiveButton("Cur cosc i bhfeidhm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DisplayUtils.generateSnackbar(getActivity(), "Cuireadh cosc ar " + LanguageUtils.lenite(blockee) + ".");
                    }
                })
                .setNegativeButton("Ná cur", null)
                .show();
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
                        imageView.setImageBitmap(BitmapUtils.getCroppedCircle(response));
                    }

                    @Override
                    public void onFailure() {
                        System.out.println("dl failure on chat pic");
                    }
                }, url, true).execute();
            }
        };
    }
}
