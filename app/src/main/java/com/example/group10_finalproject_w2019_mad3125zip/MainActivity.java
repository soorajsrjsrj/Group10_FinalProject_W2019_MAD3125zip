package com.example.group10_finalproject_w2019_mad3125zip;

import android.accounts.AccountAuthenticatorActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.CheckResult;
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
import com.facebook.LoginStatusCallback;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.Login;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginFragment;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.szagurskii.patternedtextwatcher.PatternedTextWatcher;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
    Button b1;
    IShopAppApi mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mService = Common.getAPI();


        b1 = (Button) findViewById(R.id.btn_continue);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginPage(LoginType.PHONE);
            }
        });

    }

    private void startLoginPage(LoginType loginType) {
        Intent intent = new Intent(this,AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,builder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result.getError()!=null){
                Toast.makeText(this,""+result.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT).show();

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