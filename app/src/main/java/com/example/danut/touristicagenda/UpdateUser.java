package com.example.danut.touristicagenda;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UpdateUser extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;

    private DatabaseReference databaseRefUp;

    //declare variables
    private EditText fNameUserUp, lNameUserUp, phoneUserUp;
    private TextView tVUserNameUp, emailUserUp;

    private String fName_UserUp, lName_UserUp, phone_UserUp, email_UserUp;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Update user profile");

        progressDialog = new ProgressDialog(UpdateUser.this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //Retrieve from Users database and load user details into the edit texts
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //Upload updated data into Users table
        databaseRefUp = FirebaseDatabase.getInstance().getReference("Users");

        tVUserNameUp = findViewById(R.id.tvUserNameUp);

        //initialise variables
        fNameUserUp = findViewById(R.id.etFirstNameUp);
        lNameUserUp = findViewById(R.id.etLastNameUp);
        phoneUserUp = findViewById(R.id.etPhoneUp);
        emailUserUp = findViewById(R.id.tvEmailUp);

        emailUserUp.setOnClickListener(view -> alertChangeEmailPlace());

        //save user details in the database
        Button btn_SaveUp = findViewById(R.id.btnSaveUp);
        btn_SaveUp.setOnClickListener(v -> updateUserDetails());
    }

    public void updateUserDetails() {

        if (validateUserUpdateData()) {

            progressDialog.setTitle("Updating user details!!");
            progressDialog.show();

            fName_UserUp = fNameUserUp.getText().toString().trim();
            lName_UserUp = lNameUserUp.getText().toString().trim();
            phone_UserUp = phoneUserUp.getText().toString().trim();
            email_UserUp = emailUserUp.getText().toString().trim();

            databaseRefUp.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                        if (firebaseUser.getUid().equals(postSnapshot.getKey())) {
                            postSnapshot.getRef().child("user_firstName").setValue(fName_UserUp);
                            postSnapshot.getRef().child("user_lastName").setValue(lName_UserUp);
                            postSnapshot.getRef().child("user_phoneNumber").setValue(phone_UserUp);
                            postSnapshot.getRef().child("user_emailAddress").setValue(email_UserUp);
                        }
                    }

                    progressDialog.dismiss();

                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                    TextView text = layout.findViewById(R.id.tvToast);
                    ImageView imageView = layout.findViewById(R.id.imgToast);
                    text.setText("Your details have been successfully updated!!");
                    imageView.setImageResource(R.drawable.baseline_person_add_alt_1_24);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();

                    startActivity(new Intent(UpdateUser.this, UserPage.class));
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateUser.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public Boolean validateUserUpdateData() {

        boolean result = false;

        fName_UserUp = fNameUserUp.getText().toString().trim();
        lName_UserUp = lNameUserUp.getText().toString().trim();
        phone_UserUp = phoneUserUp.getText().toString().trim();

        if (TextUtils.isEmpty(fName_UserUp)) {
            fNameUserUp.setError("Enter your first Name");
            fNameUserUp.requestFocus();
        } else if (TextUtils.isEmpty(lName_UserUp)) {
            lNameUserUp.setError("Enter your last Name");
            lNameUserUp.requestFocus();
        } else if (TextUtils.isEmpty(phone_UserUp)) {
            phoneUserUp.setError("Enter your phone number");
            fNameUserUp.requestFocus();
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUserData();
    }

    public void loadUserData() {

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    Users user_Data = postSnapshot.getValue(Users.class);

                    assert user_Data != null;
                    if (firebaseUser.getUid().equals(postSnapshot.getKey())) {
                        fNameUserUp.setText(user_Data.getUser_firstName());
                        lNameUserUp.setText(user_Data.getUser_lastName());
                        phoneUserUp.setText(user_Data.getUser_phoneNumber());
                        emailUserUp.setText(user_Data.getUser_emailAddress());
                        tVUserNameUp.setText("Edit profile of: " + user_Data.getUser_firstName() + " " + user_Data.getUser_lastName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateUser.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void alertChangeEmailPlace() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UpdateUser.this);
        alertDialogBuilder
                .setTitle("Changing user email!!")
                .setMessage("The email address cannot be change here.\nPlease use Change Email option.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_user, menu);
        return true;
    }

    public void updateUserGoBack() {
        startActivity(new Intent(UpdateUser.this, UserPage.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.updateUser_goBack) {
            updateUserGoBack();
        }

        return super.onOptionsItemSelected(item);
    }
}