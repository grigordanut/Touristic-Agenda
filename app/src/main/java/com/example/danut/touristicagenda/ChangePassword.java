package com.example.danut.touristicagenda;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private TextView tVUserAuthPass;

    private EditText etEmail, etOldPassword, etNewPassword, etConfNewPassword;

    private String user_Email, old_Password, new_Password, conf_NewPassword;

    private ProgressDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Change user password");

        progressDialog = new ProgressDialog(ChangePassword.this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        etEmail = findViewById(R.id.etUserEmailPass);
        etEmail.setEnabled(false);
        etOldPassword = findViewById(R.id.etUserOldPass);
        etNewPassword = findViewById(R.id.etUserNewPass);
        etConfNewPassword = findViewById(R.id.etUserConfNewPass);

        tVUserAuthPass = findViewById(R.id.tvUserAuthPass);
        tVUserAuthPass.setText("Your profile is not authenticated yet. Please authenticate your profile first and then change the Password!!");
        tVUserAuthPass.setTextColor(Color.RED);

        user_Email = firebaseUser.getEmail();
        etEmail.setText(user_Email);

        Button btn_ChangePassword = findViewById(R.id.btnUserChangePass);
        btn_ChangePassword.setOnClickListener(view -> alertUserPasswordNotAuth());

        Button btn_AuthUserPass = findViewById(R.id.btnAuthUserPass);
        btn_AuthUserPass.setOnClickListener(view -> {

            user_Email = etEmail.getText().toString().trim();
            old_Password = etOldPassword.getText().toString().trim();

            if (TextUtils.isEmpty(old_Password)) {
                etOldPassword.setError("Enter your password");
                etOldPassword.requestFocus();
            } else {

                progressDialog.setTitle("User authentication!!");
                progressDialog.show();

                AuthCredential credential = EmailAuthProvider.getCredential(user_Email, old_Password);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        progressDialog.dismiss();

                        tVUserAuthPass.setText("Your profile is authenticated.\nNow you can change the Password!");
                        tVUserAuthPass.setTextColor(Color.BLACK);

                        etOldPassword.setEnabled(false);
                        btn_AuthUserPass.setEnabled(false);
                        etNewPassword.requestFocus();

                        btn_ChangePassword.setOnClickListener(view1 -> {

                            new_Password = etNewPassword.getText().toString().trim();
                            conf_NewPassword = etConfNewPassword.getText().toString().trim();

                            if (TextUtils.isEmpty(new_Password)) {
                                etNewPassword.setError("Enter your new Password");
                                etNewPassword.requestFocus();
                            } else if (new_Password.length() < 6) {
                                etNewPassword.setError("Password too short.\nEnter minimum 6 character long");
                            } else if (TextUtils.isEmpty(conf_NewPassword)) {
                                etConfNewPassword.setError("Enter your new Confirm Password");
                                etConfNewPassword.requestFocus();
                            } else if (!conf_NewPassword.equals(new_Password)) {
                                etConfNewPassword.setError("The Password does not match");
                                etConfNewPassword.requestFocus();
                            } else if (new_Password.matches(old_Password)) {
                                etNewPassword.setError("Please enter a new Password\nNew Password cannot same as old");
                                etConfNewPassword.setError("Please enter a new Password\nNew Password cannot same as old");
                            } else {

                                updateUserPassword();
                            }
                        });

                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            etOldPassword.setError("Invalid Password");
                            etOldPassword.requestFocus();
                        } catch (Exception e) {
                            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressDialog.dismiss();
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void updateUserPassword() {

        progressDialog.setTitle("Changing user password!!");
        progressDialog.show();

        firebaseUser.updatePassword(new_Password).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {

                firebaseAuth.signOut();

                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                TextView text = layout.findViewById(R.id.tvToast);
                ImageView imageView = layout.findViewById(R.id.imgToast);
                text.setText("Password has been changed successfully!!");
                imageView.setImageResource(R.drawable.baseline_security_update_good_24);
                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();

                startActivity(new Intent(ChangePassword.this, LoginUser.class));
                finish();

            } else {
                try {
                    throw Objects.requireNonNull(task1.getException());
                } catch (Exception e) {
                    Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            progressDialog.dismiss();
        });
    }

    private void alertUserPasswordNotAuth() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChangePassword.this);
        alertDialogBuilder
                .setTitle("Unauthenticated user!!")
                .setMessage("Your profile is not authenticated yet.\nPlease authenticate your profile first and then change the Password!!")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_user_password, menu);
        return true;
    }

    public void changePassGoBack() {
        startActivity(new Intent(ChangePassword.this, UserPage.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.changeUserPassGoBack) {
            changePassGoBack();
        }

        return super.onOptionsItemSelected(item);
    }
}