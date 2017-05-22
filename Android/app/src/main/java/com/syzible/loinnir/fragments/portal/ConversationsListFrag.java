package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.objects.Conversation;

import java.util.ArrayList;

/**
 * Created by ed on 07/05/2017.
 */

public class ConversationsListFrag extends Fragment {

    private ArrayList<Conversation> conversations = new ArrayList<>();
    private DialogsList dialogsList;
    private DialogsListAdapter dialogsListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversations_list_frag, container, false);

        dialogsListAdapter = new DialogsListAdapter<>(loadImage());
        dialogsList.setAdapter(dialogsListAdapter);

        getActivity().setTitle(getResources().getString(R.string.app_name));
        return view;
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
