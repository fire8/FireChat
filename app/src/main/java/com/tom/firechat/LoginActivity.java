package com.tom.firechat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_GOOGLE_SIGN_IN = 100;
    private EditText userid;
    private EditText passwd;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String userUID;
    private GoogleSignInOptions gso;
    private GoogleApiClient googleApiClient;
    private CallbackManager callback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        callback = CallbackManager.Factory.create();
        loginButton.registerCallback(callback, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess");
                handleFacebookToken(loginResult);
            }



            @Override
            public void onCancel() {
                Log.d(TAG, "onSuccess");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onSuccess");
            }
        });


        gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_application_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();


        findViews();
        auth = FirebaseAuth.getInstance();
        //                            userUID =  user.getUid();
        authStateListener = new FirebaseAuth.AuthStateListener() {
           @Override
           public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               FirebaseUser user = firebaseAuth.getCurrentUser();
               if (user!=null) {
                   Log.d("onAuthStateChanged", "登入:"+
                           user.getUid());
                   userUID =  user.getUid();
               }else{
                   Log.d("onAuthStateChanged", "已登出");
               }

           }
       };


    }

    private void handleFacebookToken(LoginResult loginResult) {
        AccessToken token = loginResult.getAccessToken();
        AuthCredential credential =
            FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,
                                    "Firebase/Facebook Sign In OK",Toast.LENGTH_LONG)
                                    .show();

                        }
                    }
                });
    }

    public void google(View v){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GOOGLE_SIGN_IN){
            GoogleSignInResult signInResult =
                Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (signInResult.isSuccess()){
                GoogleSignInAccount account = signInResult.getSignInAccount();
                Log.d(TAG, "Google Sign-in 成功");
                Log.d(TAG, "Google Sign-in :"+account.getDisplayName());
                Log.d(TAG, "Google Sign-in :"+account.getEmail());
                Log.d(TAG, "Google Sign-in :"+account.getIdToken());
                firebaseAuthWithGoogle(account);

            }else{
                Log.d(TAG, "Google Sign-in 失敗");
            }
            /*if (resultCode == RESULT_OK){

            }else{

            }*/
        }else{ //facebook
            callback.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle");
        AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this,
                                    "Firebase登入成功", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    public void test(View v){
        if (userUID!=null){
//            addData();
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference userRef = db.getReference("users");
            DatabaseReference friendsRef =
                    userRef.child(userUID).child("friends").push();
            Map<String, Object> friend = new HashMap<>();
            friend.put("name", "Jack");
            friend.put("phone", "1222333");
            friendsRef.setValue(friend);
            String friendId = friendsRef.getKey();
            Log.d("FRIEND", friendId+"");
        }
    }

    private void addData() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users");
        ref.child(userUID).child("phone").setValue("9988877");
        ref.child(userUID).child("address").setValue("Taipei");
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }

    public void login(View v){
        final String uid = userid.getText().toString();
        final String pw = passwd.getText().toString();
        auth.signInWithEmailAndPassword(uid, pw).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("LOGIN", "Success");
                        }else{
                            Log.d("LOGIN", "Failed");
                            register(uid, pw);
                        }
                    }
                }
        );

    }

    private void register(final String uid, final String pw) {
        new AlertDialog.Builder(this)
                .setMessage("登入失敗，您要註冊嗎？")
                .setPositiveButton("註冊", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createUser(uid, pw);
                    }
                })
                .show();
    }

    private void createUser(String uid, String pw) {
        auth.createUserWithEmailAndPassword(uid, pw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete()){
                            Toast.makeText(LoginActivity.this, "註冊完成", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void findViews() {
        userid = (EditText) findViewById(R.id.userid);
        passwd = (EditText) findViewById(R.id.passwd);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }
}
