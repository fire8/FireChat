package com.tom.firechat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText userid;
    private EditText passwd;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String userUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
}
