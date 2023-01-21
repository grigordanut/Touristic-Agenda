package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChangeEmail extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private TextView tVUserAuthEmail;

    private EditText etOldEmail, etPassword, etNewEmail;

    private String old_Email, user_Password, new_Email;

    private ProgressDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        Objects.requireNonNull(getSupportActionBar()).setTitle("CUSTOMER: Change Email");

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        etOldEmail = findViewById(R.id.etUserOldEmail);
        etOldEmail.setEnabled(false);
        etPassword = findViewById(R.id.etUserPassword);
        etNewEmail = findViewById(R.id.etUserNewEmail);

        tVUserAuthEmail = findViewById(R.id.tvUserAuthEmail);
        tVUserAuthEmail.setText("Your profile is not authenticated yet. Please authenticate your profile first and then change the Email!!");
        tVUserAuthEmail.setTextColor(Color.RED);

        old_Email = firebaseUser.getEmail();
        etOldEmail.setText(old_Email);

        Button btn_ChangeEmail = findViewById(R.id.btnUserChangeEmail);
        btn_ChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertUserNotAuthEmail();
            }
        });

        Button btn_AuthUserEmail = findViewById(R.id.btnAuthUserEmail);
        btn_AuthUserEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                old_Email =  etOldEmail.getText().toString().trim();
                user_Password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(user_Password)) {
                    etPassword.setError("Enter your password");
                    etPassword.requestFocus();
                }
                else{

                    progressDialog.setMessage("The User is authenticating!");
                    progressDialog.show();

                    AuthCredential credential = EmailAuthProvider.getCredential(old_Email, user_Password);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                progressDialog.dismiss();

                                tVUserAuthEmail.setText("Your profile is authenticated.\nNow you can change the Email!");
                                tVUserAuthEmail.setTextColor(Color.BLACK);

                                etPassword.setEnabled(false);

                                etPassword.setOnKeyListener(new View.OnKeyListener() {
                                    @Override
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        alertPassChangeEmail();
                                        etNewEmail.requestFocus();
                                        return true;
                                    }
                                });

                                btn_AuthUserEmail.setEnabled(false);
                                btn_AuthUserEmail.setText("Disabled");
                                etNewEmail.requestFocus();

                                btn_ChangeEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        new_Email = etNewEmail.getText().toString().trim();

                                        if (TextUtils.isEmpty(new_Email)){
                                            etNewEmail.setError("Enter your new Email Address");
                                            etNewEmail.requestFocus();
                                        }
                                        else if (!Patterns.EMAIL_ADDRESS.matcher(new_Email).matches()) {
                                            etNewEmail.setError("Enter a valid Email Address");
                                            etNewEmail.requestFocus();
                                        }
                                        else{

                                            progressDialog.setMessage("The User email is changing!");
                                            progressDialog.show();

                                            firebaseUser.updateEmail(new_Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        sendEmailVerification();
                                                    }

                                                    else{
                                                        try{
                                                            throw Objects.requireNonNull(task.getException());
                                                        } catch (Exception e) {
                                                            Toast.makeText(ChangeEmail.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            else{
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthInvalidCredentialsException e){
                                    etPassword.setError("Invalid Password");
                                    etPassword.requestFocus();
                                    tVUserAuthEmail.setText("Your profile is not authenticated yet. Please authenticate your profile first and then change the email!!");
                                    tVUserAuthEmail.setTextColor(Color.RED);
                                } catch (Exception e) {
                                    Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void alertUserNotAuthEmail(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Your profile is not authenticated yet.\nPlease authenticate your profile first and then change the Email!!")
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void alertPassChangeEmail(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Password cannot be changed after user authentication!")
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void sendEmailVerification() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendUserChangeEmailData();
                    } else {
                        Toast.makeText(ChangeEmail.this, "Email verification  has not been sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserChangeEmailData() {

        new_Email = etNewEmail.getText().toString().trim();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    final FirebaseUser user_Key = firebaseAuth.getCurrentUser();

                    if (user_Key != null) {
                        if (user_Key.getUid().equals(postSnapshot.getKey())){
                            postSnapshot.getRef().child("user_emailAddress").setValue(new_Email);
                        }
                    }
                }

                progressDialog.dismiss();
                Toast.makeText(ChangeEmail.this, "Email was changed. Email verification has been sent", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                startActivity(new Intent(ChangeEmail.this, LoginUser.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChangeEmail.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_change_email, menu);
        return true;
    }

    private void changeEmailGoBack(){
        startActivity(new Intent(ChangeEmail.this, UserPage.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.userChangeEmailGoBack) {
            changeEmailGoBack();
        }

        return super.onOptionsItemSelected(item);
    }
}