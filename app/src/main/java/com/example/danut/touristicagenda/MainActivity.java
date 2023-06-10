package com.example.danut.touristicagenda;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int DELAY_MILLISECONDS = 6000;

    private ImageView imgView;
    private Animation animationRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imageView);

        //Rotate animation
        Handler handlerRotate = new Handler();
        handlerRotate.post(() -> {
            animationRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
            imgView.startAnimation(animationRotate);
        });

        //move animation
        Handler handlerDelay = new Handler();
        handlerDelay.postDelayed(() -> startActivity(new Intent(MainActivity.this, LoginUser.class)), DELAY_MILLISECONDS);

        imgView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LoginUser.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
