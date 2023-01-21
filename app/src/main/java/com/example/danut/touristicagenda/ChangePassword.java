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

        Objects.requireNonNull(getSupportActionBar()).setTitle("CUSTOMER: change Password");

        progressDialog = new ProgressDialog(this);

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

        Button buttonChangePassword = findViewById(R.id.btnUserChangePass);
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertUserNotAuthPassword();
            }
        });

        Button buttonAuthUserPass = findViewById(R.id.btnAuthUserPass);
        buttonAuthUserPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                user_Email =  etEmail.getText().toString().trim();
                old_Password = etOldPassword.getText().toString().trim();

                if (TextUtils.isEmpty(old_Password)) {
                    etOldPassword.setError("Enter your password");
                    etOldPassword.requestFocus();
                }
                else{

                    progressDialog.setMessage("The User is authenticating!");
                    progressDialog.show();

                    AuthCredential credential = EmailAuthProvider.getCredential(user_Email, old_Password);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                progressDialog.dismiss();

                                tVUserAuthPass.setText("Your profile is authenticated.\nNow you can change the Password!");
                                tVUserAuthPass.setTextColor(Color.BLACK);

                                etOldPassword.setOnKeyListener(new View.OnKeyListener() {
                                    @Override
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        alertPassChangePassword();
                                        etNewPassword.requestFocus();
                                        return true;
                                    }
                                });

                                etOldPassword.setEnabled(false);
                                buttonAuthUserPass.setEnabled(false);
                                buttonAuthUserPass.setText("Disabled");
                                etNewPassword.requestFocus();

                                buttonChangePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        new_Password = etNewPassword.getText().toString().trim();
                                        conf_NewPassword = etConfNewPassword.getText().toString().trim();

                                        if (TextUtils.isEmpty(new_Password)){
                                            etNewPassword.setError("Enter your new Password");
                                            etNewPassword.requestFocus();
                                        }
                                        else if (new_Password.length() < 6) {
                                            etNewPassword.setError("The password is too short.\nEnter minimum 6 character long");
                                            etNewPassword.requestFocus();
                                        }
                                        else if (TextUtils.isEmpty(conf_NewPassword)) {
                                            etConfNewPassword.setError("Confirm your new Password");
                                            etConfNewPassword.requestFocus();
                                        }
                                        else if (!conf_NewPassword.equals(new_Password)) {
                                            etConfNewPassword.setError("The Password does not match");
                                            etConfNewPassword.requestFocus();
                                        }
                                        else{

                                            progressDialog.setMessage("The User password is changing!");
                                            progressDialog.show();

                                            firebaseUser.updatePassword(new_Password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){

                                                        firebaseAuth.signOut();
                                                        Toast.makeText(ChangePassword.this, "The password will be changed.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(ChangePassword.this, LoginUser.class));
                                                        finish();

                                                    }

                                                    else{
                                                        try {
                                                            throw Objects.requireNonNull(task.getException());
                                                        } catch (Exception e) {
                                                            Toast.makeText(ChangePassword.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    progressDialog.dismiss();
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
                                    etOldPassword.setError("Invalid Password");
                                    etOldPassword.requestFocus();
                                    tVUserAuthPass.setText("Your profile is not authenticated yet. Please authenticate your profile first and then change the Password!!");
                                    tVUserAuthPass.setTextColor(Color.RED);
                                } catch (Exception e) {
                                    Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void alertUserNotAuthPassword(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Your profile is not authenticated yet.\nPlease authenticate your profile first and then change the Password!!")
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

    private void alertPassChangePassword(){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_change_password, menu);
        return true;
    }

    private void changePassGoBack(){
        startActivity(new Intent(ChangePassword.this, UserPage.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.userChangePassGoBack) {
            changePassGoBack();
        }

        return super.onOptionsItemSelected(item);
    }
}