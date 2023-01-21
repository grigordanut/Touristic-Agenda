package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginUser extends AppCompatActivity {

    private TextInputEditText emailLogUser;
    private TextInputEditText passLogUser;

    private String email_logUser, pass_logUser;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Login Users");

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        emailLogUser = findViewById(R.id.etEmailLog);
        passLogUser = findViewById(R.id.etPassLog);

        CheckBox rememberCheckBox = findViewById(R.id.rememberCB);

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("remember", "");

        if (checkbox != null) {
            if (checkbox.equals("true")) {
                Intent intent = new Intent(LoginUser.this, UserPage.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginUser.this, "Please Sign In", Toast.LENGTH_SHORT).show();
            }
        }

        rememberCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(LoginUser.this, "Remember me Enabled", Toast.LENGTH_SHORT).show();
                } else if (!compoundButton.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(LoginUser.this, "Remember me Disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btn_regUser = findViewById(R.id.btnRegisterLog);
        btn_regUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regUser = new Intent(LoginUser.this, RegisterUser.class);
                startActivity(regUser);
            }
        });

        TextView tVForgotPass = findViewById(R.id.tvForgotPass);
        tVForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotPass = new Intent(LoginUser.this, ResetPassword.class);
                startActivity(forgotPass);
            }
        });

        //log in Users
        Button btn_loginUser = findViewById(R.id.btnUserLog);
        btn_loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateUserLogData()) {

                    progressDialog.setMessage("Login Users");
                    progressDialog.show();

                    firebaseAuth.signInWithEmailAndPassword(email_logUser, pass_logUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                checkEmailVerification();

                            } else {
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthInvalidUserException e) {
                                    emailLogUser.setError("This email is not registered.");
                                    emailLogUser.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    passLogUser.setError("Invalid Password");
                                    passLogUser.requestFocus();
                                } catch (Exception e) {
                                    Toast.makeText(LoginUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private Boolean validateUserLogData() {
        boolean result = false;

        email_logUser =(Objects.requireNonNull(emailLogUser.getText())).toString().trim();
        pass_logUser = Objects.requireNonNull(passLogUser.getText()).toString().trim();

        if (email_logUser.isEmpty()) {
            emailLogUser.setError("Enter your Login Email");
            emailLogUser.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_logUser).matches()) {
            emailLogUser.setError("Enter a valid Email Address");
            emailLogUser.requestFocus();
        } else if (pass_logUser.isEmpty()) {
            passLogUser.setError("Enter your Login Password");
            passLogUser.requestFocus();
        } else {
            result = true;
        }
        return result;
    }

    //check if the email has been verified
    private void checkEmailVerification() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {

            if (firebaseUser.isEmailVerified()) {

                Toast.makeText(LoginUser.this, "Users successfully Log in!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginUser.this, UserPage.class));
                finish();
            } else {
                Toast.makeText(this, "Please verify your Email first.", Toast.LENGTH_SHORT).show();
            }

            progressDialog.dismiss();
        }
    }
}