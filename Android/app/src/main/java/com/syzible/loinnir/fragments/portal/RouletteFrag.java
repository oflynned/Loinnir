package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.syzible.loinnir.R;
import com.syzible.loinnir.network.Endpoints;
import com.syzible.loinnir.network.RestClient;
import com.syzible.loinnir.objects.User;
import com.syzible.loinnir.utils.DisplayUtils;
import com.syzible.loinnir.utils.EmojiUtils;
import com.syzible.loinnir.utils.LanguageUtils;
import com.syzible.loinnir.utils.LocalStorage;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by ed on 07/05/2017.
 */

public class RouletteFrag extends Fragment {

    private ImageView rouletteButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roulette_frag, container, false);
        getActivity().setTitle(getResources().getString(R.string.app_name));

        rouletteButton = (ImageView) view.findViewById(R.id.roulette_spinner_button);
        rouletteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rouletteButton.clearAnimation();
                rouletteButton.animate().rotation(360).start();

                JSONObject payload = new JSONObject();
                try {
                    payload.put("fb_id", LocalStorage.getID(getActivity()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                RestClient.post(getActivity(), Endpoints.GET_RANDOM_USER, payload, new BaseJsonHttpResponseHandler<JSONObject>() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                        try {
                            if (response.has("success")) {
                                DisplayUtils.generateSnackbar(getActivity(), "Níl aon úsáideoirí nua ann le nascadh " + EmojiUtils.getEmoji(EmojiUtils.SAD));
                            } else {
                                String name = response.getString("name").split(" ")[0];
                                DisplayUtils.generateSnackbar(getActivity(), "Haigh, a " + LanguageUtils.getVocative(name) + "! Nascadh thú leis an úsáideoir seo " + EmojiUtils.getEmoji(EmojiUtils.HAPPY));
                                matchPartner(new User(response));
                            }
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
        });

        return view;
    }

    private void matchPartner(User partner) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("my_id", LocalStorage.getID(getActivity()));
            payload.put("partner_id", partner.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RestClient.post(getActivity(), Endpoints.SUBSCRIBE_TO_PARTNER, payload, new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                System.out.println(rawJsonResponse);
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
}
