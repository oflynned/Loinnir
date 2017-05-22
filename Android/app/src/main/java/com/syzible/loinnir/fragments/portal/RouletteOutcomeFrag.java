package com.syzible.loinnir.fragments.portal;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class RouletteOutcomeFrag extends Fragment {

    private ImageView profilePictureImageView;
    private TextView usernameTextView, countyTextView;
    private Button backToRouletteButton, startConversationButton;

    private User partner;
    private Bitmap profilePic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roulette_outcome_frag, container, false);
        getActivity().setTitle(getResources().getString(R.string.app_name));

        profilePictureImageView = (ImageView) view.findViewById(R.id.roulette_partner_profile_pic);
        usernameTextView = (TextView) view.findViewById(R.id.name_text_roulette);
        countyTextView = (TextView) view.findViewById(R.id.county_text_roulette);

        backToRouletteButton = (Button) view.findViewById(R.id.back_to_roulette_btn);
        backToRouletteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.removeFragment(getFragmentManager());
                MainActivity.setFragment(getFragmentManager(), new RouletteFrag());
            }
        });

        startConversationButton = (Button) view.findViewById(R.id.start_conversation_btn);
        startConversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matchPartner(partner);
                MainActivity.removeFragment(getFragmentManager());
                DisplayUtils.generateSnackbar(getActivity(), "Start convo");
            }
        });

        new GetImage(new NetworkCallback<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                Bitmap croppedBitmap = BitmapUtils.getCroppedCircle(response);
                profilePictureImageView.setImageBitmap(croppedBitmap);
            }

            @Override
            public void onFailure() {

            }
        }, partner.getAvatar(), true).execute();

        usernameTextView.setText(partner.getName());
        countyTextView.setText(partner.getLocality());

        return view;
    }

    public RouletteOutcomeFrag setPartner(User partner) {
        this.partner = partner;
        return this;
    }

    public RouletteOutcomeFrag setBitmap(Bitmap profilePic) {
        this.profilePic = profilePic;
        return this;
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
