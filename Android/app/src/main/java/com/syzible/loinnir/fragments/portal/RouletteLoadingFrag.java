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
import com.syzible.loinnir.R;
import com.syzible.loinnir.activities.MainActivity;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.GetImage;
import com.syzible.loinnir.network.NetworkCallback;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.utils.BitmapUtils;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.EmojiUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class RouletteLoadingFrag extends Fragment {

    private ImageView rouletteButton;
    private User partner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roulette_frag, container, false);
        getActivity().setTitle(getResources().getString(R.string.app_name));

        rouletteButton = (ImageView) view.findViewById(R.id.roulette_spinner_button);

        rouletteButton.clearAnimation();
        rouletteButton.animate().rotation(360).start();

        new GetImage(new NetworkCallback<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                RouletteOutcomeFrag matchFrag = new RouletteOutcomeFrag()
                        .setPartner(partner)
                        .setBitmap(response);
                MainActivity.removeFragment(getFragmentManager());
                MainActivity.setFragment(getFragmentManager(), matchFrag);
            }

            @Override
            public void onFailure() {

            }
        }, partner.getAvatar(), true).execute();

        return view;
    }


    public RouletteLoadingFrag setPartner(User partner) {
        this.partner = partner;
        return this;
    }
}
