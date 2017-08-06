package com.group06.applications.uoclbsp_source;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void signUp(View view){
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }

    public void login(View view){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }
}
