package com.tom.firechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText userid;
    private EditText passwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
    }

    public void login(View v){
        String uid = userid.getText().toString();
        String pw = passwd.getText().toString();
        

    }

    private void findViews() {
        userid = (EditText) findViewById(R.id.userid);
        passwd = (EditText) findViewById(R.id.passwd);
    }
}
