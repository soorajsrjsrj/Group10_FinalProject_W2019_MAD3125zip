package com.example.group10_finalproject_w2019_mad3125zip;

import android.Manifest;
import android.accounts.AccountAuthenticatorActivity;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Message;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


//facebook library needed for the login process
import com.example.group10_finalproject_w2019_mad3125zip.Model.CheckUserResponse;
import com.example.group10_finalproject_w2019_mad3125zip.Model.User;
import com.example.group10_finalproject_w2019_mad3125zip.Retrofit.IShopAppApi;
import com.example.group10_finalproject_w2019_mad3125zip.Utils.Common;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;

import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.szagurskii.patternedtextwatcher.PatternedTextWatcher;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private int nextPermissionsRequestCode = 4000;
    private final Map<Integer, OnCompleteListener> permissionsListeners = new HashMap<>();
    Button b1;
    IShopAppApi mService;
    Context context;
    CallbackManager callbackManager;

    private interface OnCompleteListener {
        void onComplete();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


      //  mService = Common.getAPI();




        context = getBaseContext();
        callbackManager = CallbackManager.Factory.create(); //facebook authentication





        b1 = (Button) findViewById(R.id.btn_continue);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginPage(LoginType.PHONE);
            }
        });

    }


    //copy start
    public void onLoginPhone(final View view) {
        if(AccountKit.getCurrentAccessToken() != null){
            Toast.makeText(context, "You Already Logged In", Toast.LENGTH_SHORT).show();
        }else {
            startLoginPage(LoginType.PHONE);
           // progress_bar_parent.setVisibility(View.VISIBLE);
        }
    }
    //facebook kit auth
    private void startLoginPage(final LoginType loginType) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        final AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType, AccountKitActivity.ResponseType.TOKEN);
        final AccountKitConfiguration configuration = configurationBuilder.build();
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
        OnCompleteListener completeListener = new OnCompleteListener() {
            @Override
            public void onComplete() {
                startActivityForResult(intent, REQUEST_CODE);
            }
        };
        switch (loginType) {
            case EMAIL:
                if (!isGooglePlayServicesAvailable()) {
                    final OnCompleteListener getAccountsCompleteListener = completeListener;
                    completeListener = new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            requestPermissions(Manifest.permission.GET_ACCOUNTS, R.string.permissions_get_accounts_title, R.string.permissions_get_accounts_message, getAccountsCompleteListener);
                        }
                    };
                }
                break;
            case PHONE:
                if (configuration.isReceiveSMSEnabled() && !canReadSmsWithoutPermission()) {
                    final OnCompleteListener receiveSMSCompleteListener = completeListener;
                    completeListener = new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            requestPermissions(Manifest.permission.RECEIVE_SMS, R.string.permissions_receive_sms_title, R.string.permissions_receive_sms_message, receiveSMSCompleteListener);
                        }
                    };
                }
                if (configuration.isReadPhoneStateEnabled() && !isGooglePlayServicesAvailable()) {
                    final OnCompleteListener readPhoneStateCompleteListener = completeListener;
                    completeListener = new OnCompleteListener() {
                        @Override
                        public void onComplete() {
                            requestPermissions(Manifest.permission.READ_PHONE_STATE, R.string.permissions_read_phone_state_title, R.string.permissions_read_phone_state_message, readPhoneStateCompleteListener);
                        }
                    };
                }
                break;
        }
        completeListener.onComplete();
    }

    //facebook kit auth
    private boolean isGooglePlayServicesAvailable() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int googlePlayServicesAvailable = apiAvailability.isGooglePlayServicesAvailable(this);
        return googlePlayServicesAvailable == ConnectionResult.SUCCESS;
    }

    //facebook kit auth
    private boolean canReadSmsWithoutPermission() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int googlePlayServicesAvailable = apiAvailability.isGooglePlayServicesAvailable(this);
        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            return true;
        }

        return false;
    }

    //facebook kit auth
    private void requestPermissions(final String permission, final int rationaleTitleResourceId, final int rationaleMessageResourceId, final OnCompleteListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        checkRequestPermissions(permission, rationaleTitleResourceId, rationaleMessageResourceId, listener);
    }

    //facebook kit auth
    @TargetApi(23)
    private void checkRequestPermissions(final String permission, final int rationaleTitleResourceId, final int rationaleMessageResourceId, final OnCompleteListener listener) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        final int requestCode = nextPermissionsRequestCode++;
        permissionsListeners.put(requestCode, listener);

        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(this).setTitle(rationaleTitleResourceId).setMessage(rationaleMessageResourceId).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    requestPermissions(new String[]{permission}, requestCode);
                }
            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    // ignore and clean up the listener
                    permissionsListeners.remove(requestCode);
                }
            }).setIcon(android.R.drawable.ic_dialog_alert).show();
        } else {

            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    //facebook kit auth
    @TargetApi(23)
    @SuppressWarnings("unused")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String permissions[], final @NonNull int[] grantResults) {
        final OnCompleteListener permissionsListener = permissionsListeners.remove(requestCode);
        if (permissionsListener != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionsListener.onComplete();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    //facebook authentication login
    private void callUserAuth(LoginResult loginResult) {

        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());

                        try {
                            // Application code
                            String email = object.getString("email");
                            String birthday = object.getString("birthday"); // 01/31/1980 format
                            Log.d("myauth","\n email : "+email);

                            // aru further data handle garney aba...

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();

    }

    //    logut facebook app config
    public void onLogout(View view) {
        LoginManager.getInstance().logOut();
        AccountKit.logOut();
        if(AccountKit.getCurrentAccessToken() == null){
            Toast.makeText(context, "LogOut Complete", Toast.LENGTH_SHORT).show();
        }
    }




/*
    private void startLoginPage(LoginType loginType) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,AccountKitActivity.ResponseType.TOKEN);
      //  uiManager = new SkinManager(SkinManager.Skin.CONTEMPORARY, Color.parseColor("#EC1D24"));
      //  configurationBuilder.setUIManager(uiManager);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);

    }
*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result.getError()!=null){
                Toast.makeText(this,"inside on activity result"+result.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT).show();

            }
            else if (result.wasCancelled()){
                Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
            }
            else {
                if (result.getAccessToken()!=null){
                    final AlertDialog alertDialog = new SpotsDialog(MainActivity.this);
                    alertDialog.show();
                    alertDialog.setMessage("Please waiting ...");




                    //get user phone and check exists on server
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                           mService.checkUserExists(account.getPhoneNumber().toString())
                                    .enqueue(new Callback<CheckUserResponse>() {
                                        @Override
                                        public void onResponse(Call<CheckUserResponse> call, Response<CheckUserResponse> response) {
                                            CheckUserResponse userResponse = response.body();
                                            if (userResponse.isExists()){

                                                Toast.makeText(MainActivity.this,"userexist",Toast.LENGTH_SHORT).show();

                                                //if user already exists , just start new activity
                                                alertDialog.dismiss();

                                            }
                                            else{
                                                //else need regioster
                                                alertDialog.dismiss();
                                                shoRegisterDialog(account.getPhoneNumber().toString());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<CheckUserResponse> call, Throwable t) {





                                        }
                                    });

                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Log.d("ERROR",accountKitError.getErrorType().getMessage());

                        }
                    });



                }


            }
        }
    }

    private void shoRegisterDialog(final String phone) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Register");

        LayoutInflater inflater = this.getLayoutInflater();
        View register_layout = inflater.inflate(R.layout.register_layout,null);

        final MaterialEditText edt_name = (MaterialEditText)register_layout.findViewById(R.id.edt_name);
        final MaterialEditText edt_address = (MaterialEditText)register_layout.findViewById(R.id.edt_address);
        final MaterialEditText edt_birtdate = (MaterialEditText)register_layout.findViewById(R.id.edt_birtdate);

        Button btn_register = (Button)findViewById(R.id.btn_register);

        edt_birtdate.addTextChangedListener(new PatternedTextWatcher("####-##-##"));


        //event
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //close dialog
                alertDialog.create().dismiss();


                if (TextUtils.isEmpty(edt_address.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please enter your address",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edt_birtdate.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please enter your birtdate",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edt_name.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please enter your name",Toast.LENGTH_SHORT).show();
                    return;
                }


                final AlertDialog waitinDialog = new SpotsDialog(MainActivity.this);
                waitinDialog.show();
                waitinDialog.setMessage("Please waiting ...");





                mService.registerNewUser(phone,edt_name.getText().toString(),
                        edt_address.getText().toString(),
                        edt_birtdate.getText().toString()).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        waitinDialog.dismiss();
                        User user = response.body();

                        if (TextUtils.isEmpty(user.getError_msg())){
                            Toast.makeText(MainActivity.this,"User registration succesfully",Toast.LENGTH_SHORT).show();
                            //start new activity
                        }

                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        waitinDialog.dismiss();

                    }
                });


            }



        });


        alertDialog.setView(register_layout);
        alertDialog.show();
    }
}
