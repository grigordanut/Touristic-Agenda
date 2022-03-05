package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserPage extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListenerUser;

    private TextView tVUserPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        Objects.requireNonNull(getSupportActionBar()).setTitle("User Page");

        firebaseAuth = FirebaseAuth.getInstance();

        //initialise the variables
        tVUserPage = findViewById(R.id.tvUserPage);

        Button btn_addEvent = findViewById(R.id.btnAddEvent);
        btn_addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addEvent = new Intent(UserPage.this, AddEvent.class);
                startActivity(addEvent);
            }
        });

        Button btn_showEvents = findViewById(R.id.btnShowEvents);
        btn_showEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showEvent = new Intent(UserPage.this, EventsImage.class);
                startActivity(showEvent);
            }
        });

        //retrieve data from database into text views
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        eventListenerUser = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NewApi"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve data from database
                for (final DataSnapshot dsUser : dataSnapshot.getChildren()) {
                    FirebaseUser user_Db = firebaseAuth.getCurrentUser();
                    final User user_data = dsUser.getValue(User.class);

                    if (user_Db != null){
                        assert user_data != null;
                        if (user_Db.getUid().equals(dsUser.getKey())){
                            tVUserPage.setText("Welcome: "+user_data.getUser_firstName()+" "+user_data.getUser_lastName());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserPage.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editProfile() {
        startActivity(new Intent(UserPage.this, UpdateUser.class));
        finish();
    }

    private void changePassword() {
        startActivity(new Intent(UserPage.this, ChangePassword.class));
        finish();
    }

    //user log out
    private void LogOut() {
        SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("remember", "false");
        editor.apply();

        Toast.makeText(UserPage.this, "You are Log Out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(UserPage.this, LoginUser.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logOutUser) {
            alertDialogUserLogout();
        }

        if (item.getItemId() == R.id.editProfile) {
            editProfile();
        }

        if (item.getItemId() == R.id.changePassword) {
            changePassword();
        }

        return super.onOptionsItemSelected(item);
    }

    private void alertDialogUserLogout(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserPage.this);
        alertDialogBuilder
                .setTitle("Log out User")
                .setMessage("Are sure to Log Out?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        (dialog, id) -> LogOut())

                .setNegativeButton("No",
                        (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(eventListenerUser != null){
            databaseReference.removeEventListener(eventListenerUser);
        }
    }
}