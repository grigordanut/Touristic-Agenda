package com.example.danut.touristicagenda;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        Objects.requireNonNull(getSupportActionBar()).setTitle("Change user email");

        progressDialog = new ProgressDialog(ChangeEmail.this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

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
        btn_ChangeEmail.setOnClickListener(view -> alertUserEmailNotAuth());

        Button btn_AuthUserEmail = findViewById(R.id.btnAuthUserEmail);
        btn_AuthUserEmail.setOnClickListener(view -> {

            old_Email = etOldEmail.getText().toString().trim();
            user_Password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(user_Password)) {
                etPassword.setError("Enter your password");
                etPassword.requestFocus();
            } else {

                progressDialog.setTitle("User authentication!!");
                progressDialog.show();

                AuthCredential credential = EmailAuthProvider.getCredential(old_Email, user_Password);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        tVUserAuthEmail.setText("Your profile is authenticated.\nNow you can change the Email!");
                        tVUserAuthEmail.setTextColor(Color.BLACK);

                        etPassword.setEnabled(false);
                        btn_AuthUserEmail.setEnabled(false);
                        etNewEmail.requestFocus();

                        btn_ChangeEmail.setOnClickListener(view1 -> {

                            new_Email = etNewEmail.getText().toString().trim();

                            if (TextUtils.isEmpty(new_Email)) {
                                etNewEmail.setError("Enter your new email address");
                                etNewEmail.requestFocus();
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(new_Email).matches()) {
                                etNewEmail.setError("Enter a valid email address");
                            } else if (old_Email.matches(new_Email)) {
                                etNewEmail.setError("Please enter a new email\nNew email cannot be same as old");
                            } else {

                                updateUserEmail();
                            }
                        });

                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            etPassword.setError("Invalid password");
                            etPassword.requestFocus();
                        } catch (Exception e) {
                            Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressDialog.dismiss();
                });
            }
        });
    }

    public void updateUserEmail() {

        progressDialog.setTitle("Changing user email!!");
        progressDialog.show();

        firebaseUser.updateEmail(new_Email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        uploadUserChangeEmailData();

                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e) {
                            Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {

                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                    TextView text = layout.findViewById(R.id.tvToast);
                    ImageView imageView = layout.findViewById(R.id.imgToast);
                    text.setText(e.getMessage());
                    imageView.setImageResource(R.drawable.baseline_report_gmailerrorred_24);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                });
    }

    @SuppressLint("SetTextI18n")
    public void uploadUserChangeEmailData() {

        String user_Id = firebaseUser.getUid();

        databaseReference.child(user_Id).child("user_emailAddress").setValue(new_Email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        firebaseUser.sendEmailVerification();

                        LayoutInflater inflater = getLayoutInflater();
                        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                        TextView text = layout.findViewById(R.id.tvToast);
                        ImageView imageView = layout.findViewById(R.id.imgToast);
                        text.setText("Email has been changed successfully. Verification email has been sent!!");
                        imageView.setImageResource(R.drawable.ic_baseline_email_24);
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();

                        startActivity(new Intent(ChangeEmail.this, LoginUser.class));
                        finish();
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e) {
                            Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    public void alertUserEmailNotAuth() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChangeEmail.this);
        alertDialogBuilder
                .setTitle("Unauthenticated user!!")
                .setMessage("Your profile is not authenticated yet.\nPlease authenticate your profile first and then change the Email!!")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_user_email, menu);
        return true;
    }

    public void changeEmailGoBack() {
        startActivity(new Intent(ChangeEmail.this, UserPage.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.changeUserEmailGoBack) {
            changeEmailGoBack();
        }

        return super.onOptionsItemSelected(item);
    }
}