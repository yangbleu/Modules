package com.bleu_autom.modules.Controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

/**
 * Created by GeorgeYang on 2016/12/28.
 */

public class OauthController {
    private static String LOG_TAG = "";
    // LOG_TAG = this.getClass().getSimpleName();

    private Activity activity;
    //social login
    //private SocialAuthAdapter adapter;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    public OauthController(Activity activity){
        this.activity = activity;
        LOG_TAG = this.getClass().getSimpleName();
        init_Google_SignIn();
    }

    /**********************
     * Google social login
     **********************/
    private void init_Google_SignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.Google_Client_ID))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage((FragmentActivity)activity, ValueIndex.GOOGLE_API_CLIENT_ID++ /* clientId */, (GoogleApiClient.OnConnectionFailedListener) activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    public void Google_SignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, ValueIndex.REQUEST_GOOGLE_SIGN_IN);
    }

    public boolean handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();
            if(acct!=null){
                Log.d(LOG_TAG,"GoogleSignInAccount = "+acct.toString()
                        +"\nEmail:"+acct.getEmail()
                        +"\nId:"+acct.getId()
                        +"\nDisplayName:"+acct.getDisplayName()
                        +"\nIdToken:"+acct.getIdToken()
                        +"\nServerAuthCode:"+acct.getServerAuthCode()
                        +"\nPhotoUrl:"+acct.getPhotoUrl()
                );
                if(PersonInfoModel.local_data.user_photo_uri.isEmpty() && acct.getPhotoUrl()!=null) {
                    final Uri googlePhotoUri = acct.getPhotoUrl();
                    if(googlePhotoUri!=null){
                        Glide
                                .with(activity.getApplicationContext())
                                .load(googlePhotoUri)
                                .asBitmap()
                                .toBytes(Bitmap.CompressFormat.JPEG, 85)
                                //.toBytes()
                                .diskCacheStrategy( DiskCacheStrategy.NONE )
                                .skipMemoryCache( true )
                                .into(new SimpleTarget<byte[]>(){
                                    @Override
                                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                        Log.d(LOG_TAG, "CompressImg Glide onResourceReady uri="+googlePhotoUri);
                                        Log.d(LOG_TAG, "savePhoto resource.length="+resource.length);
                                        AddPhotoController addPhotoController = new AddPhotoController(activity,null);
                                        //save Photo
                                        String photoPath = addPhotoController.savePhoto(resource, "");
                                        if (photoPath!=null && !photoPath.isEmpty()){
                                            PersonInfoModel.local_data.user_photo_uri = photoPath;
                                            PersonInfoModel.local_data.saveData(activity);
                                        }
                                    }
                                });
                    }
                }
                PersonInfoModel.local_data.uuid = acct.getId();
                PersonInfoModel.local_data.oauth_token = acct.getIdToken();
                if(PersonInfoModel.member_data.nickname.isEmpty()) PersonInfoModel.member_data.nickname = acct.getDisplayName();
                if(PersonInfoModel.member_data.e_mail.isEmpty()) PersonInfoModel.member_data.e_mail = acct.getEmail();
                PersonInfoModel.member_data.saveData(activity);
                return true;
            }
        }
        Log.d(LOG_TAG,"google result:" + result.getStatus().toString());
        Toast.makeText(activity, "Google 登入失敗", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**********************
     * Facebook social login
     **********************/
    public CallbackManager init_FB_SignIn(final OnFinishListener listener) {
        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        AppEventsLogger.activateApp(activity.getApplication());
        Log.d(LOG_TAG,"FB:init");
        // manually config
        FacebookSdk.setApplicationId(activity.getResources().getString(R.string.facebook_app_id));

        //宣告callback Manager
        callbackManager = CallbackManager.Factory.create();

        //幫 LoginManager 加 callback function
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //登入成功
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LOG_TAG, "FB:Success");
                // accessToken存起來
                final AccessToken accessToken = loginResult.getAccessToken();

                // send request and call graph api
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            // 當RESPONSE回來的時候, 資訊存留在 object 中
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                String fb_id = object.optString("id");
                                final String FB_Photo = object.optString("picture");
                                Log.d(LOG_TAG,"FB:onCompleted\n"
                                        +"\nid:"+fb_id
                                        +"\nname:"+object.optString("name")
                                        +"\nemail:"+object.optString("email")
                                        +"\nIdToken:"+accessToken.getToken()
                                        +"\nPhotoUrl:"+FB_Photo
                                );
                                if(!fb_id.isEmpty()){
//                                    final String FB_Photo = "http://graph.facebook.com/" +
//                                            id + "/picture?type=large";
                                    if(PersonInfoModel.local_data.user_photo_uri.isEmpty() && !FB_Photo.isEmpty()){
                                        Glide
                                                .with(activity.getApplicationContext())
                                                .load(FB_Photo)
                                                .asBitmap()
                                                .toBytes(Bitmap.CompressFormat.JPEG, 85)
                                                //.toBytes()
                                                .diskCacheStrategy( DiskCacheStrategy.NONE )
                                                .skipMemoryCache( true )
                                                .into(new SimpleTarget<byte[]>(){
                                                    @Override
                                                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                                        Log.d(LOG_TAG, "CompressImg Glide onResourceReady uri="+FB_Photo);
                                                        Log.d(LOG_TAG, "savePhoto resource.length="+resource.length);
                                                        AddPhotoController addPhotoController = new AddPhotoController(activity,null);
                                                        //save Photo
                                                        String photoPath = addPhotoController.savePhoto(resource, "");
                                                        if (photoPath!=null && !photoPath.isEmpty()){
                                                            PersonInfoModel.local_data.user_photo_uri = photoPath;
                                                            PersonInfoModel.local_data.saveData(activity);
                                                        }
                                                    }
                                                });
                                    }
                                    PersonInfoModel.local_data.uuid = fb_id;
                                    PersonInfoModel.local_data.oauth_token = accessToken.getToken();
                                    if(PersonInfoModel.member_data.nickname.isEmpty()) PersonInfoModel.member_data.nickname = object.optString("name");
                                    if(PersonInfoModel.member_data.e_mail.isEmpty()) PersonInfoModel.member_data.e_mail = object.optString("email");
                                    PersonInfoModel.member_data.saveData(activity);
                                    listener.OnReceived(true,"FB:Success",null);
                                }
                            }
                        });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id,name,picture,email");
//                request.setParameters(parameters);
                request.executeAsync();
            }

            //登入取消
            @Override
            public void onCancel() {
                // App code
                Log.d(LOG_TAG, "FB:CANCEL");
                listener.OnReceived(false,"FB:CANCEL",null);
            }

            //登入失敗
            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(LOG_TAG, "FB:"+ exception.toString());
                listener.OnReceived(false,"FB:Error",null);
            }
        });
        return callbackManager;
    }

}

//    /**********************
//     * 返回結果
//     **********************/
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(LOG_TAG, "requestCode:"+requestCode+", resultCode:"+resultCode);
//        //callback data from account name
//        switch (requestCode){
//            case ValuIndex.REQUEST_GOOGLE_SIGN_IN: // Google callback
//                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//                if(oauthController.handleSignInResult(result)) {
//                    myProgressDialog.start(context, "登入", MyProgressDialog.TIMEOUT);
//                    PersonInfoDB.getInstance().do_login_oauth(mThis, new OnFinishListener() {
//                        @Override
//                        public void OnReceived(boolean success, String message, @Nullable Integer index) {
//                            myProgressDialog.dismiss();
//                            if (success){
//                                do_login_check();
//                            }
//                            Toolbase.tostshow(mThis,message,true);
//                        }
//                    },"google");
//                }
//                break;
//            case ValuIndex.REQUEST_FB_SIGN_IN: // Facebook callback fixed 64206
//                callbackManager.onActivityResult(requestCode, resultCode, data);
//                break;
//            default:
//                break;
//        }
//    }