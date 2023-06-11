package com.example.danut.touristicagenda;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Login User");

        progressDialog = new ProgressDialog(LoginUser.this);

        firebaseAuth = FirebaseAuth.getInstance();

        emailLogUser = findViewById(R.id.etEmailLog);
        passLogUser = findViewById(R.id.etPassLog);

        CheckBox rememberCheckBox = findViewById(R.id.rememberCB);

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("remember", "");

        if (checkbox.equals("true")) {
            startActivity(new Intent(LoginUser.this, UserPage.class));

        } else {
            Toast.makeText(LoginUser.this, "Please Sign In", Toast.LENGTH_SHORT).show();
        }

        rememberCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("remember", "true");
                editor.apply();
                Toast.makeText(LoginUser.this, "Remember me Enabled", Toast.LENGTH_SHORT).show();
            } else if (!compoundButton.isChecked()) {
                SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("remember", "false");
                editor.apply();
                Toast.makeText(LoginUser.this, "Remember me Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        Button btn_regUser = findViewById(R.id.btnRegisterLog);
        btn_regUser.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, RegisterUser.class)));

        TextView tVForgotPass = findViewById(R.id.tvForgotPass);
        tVForgotPass.setOnClickListener(v -> startActivity(new Intent(LoginUser.this, ResetPassword.class)));

        //log in Users
        Button btn_loginUser = findViewById(R.id.btnUserLog);
        btn_loginUser.setOnClickListener(v -> logInUser());
    }

    public void logInUser() {
        if (validateUserLogData()) {

            progressDialog.setTitle("Login User!!");
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email_logUser, pass_logUser).addOnCompleteListener(task -> {
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
            });
        }
    }

    //check if the email has been verified
    @SuppressLint("SetTextI18n")
    private void checkEmailVerification() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        assert firebaseUser != null;
        if (firebaseUser.isEmailVerified()) {

            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
            TextView text = layout.findViewById(R.id.tvToast);
            ImageView imageView = layout.findViewById(R.id.imgToast);
            text.setText("Login Successful!!");
            imageView.setImageResource(R.drawable.ic_baseline_login_24);
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

            Intent intent = new Intent(LoginUser.this, UserPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else {

            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
            TextView text = layout.findViewById(R.id.tvToast);
            ImageView imageView = layout.findViewById(R.id.imgToast);
            text.setText("Please verify your Email!!");
            imageView.setImageResource(R.drawable.baseline_report_gmailerrorred_24);
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

    private Boolean validateUserLogData() {

        boolean result = false;

        email_logUser = (Objects.requireNonNull(emailLogUser.getText())).toString().trim();
        pass_logUser = Objects.requireNonNull(passLogUser.getText()).toString().trim();

        if (email_logUser.isEmpty()) {
            emailLogUser.setError("Enter your Login Email");
            emailLogUser.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_logUser).matches()) {
            emailLogUser.setError("Enter a valid Email Address");
        } else if (pass_logUser.isEmpty()) {
            passLogUser.setError("Enter your Login Password");
            passLogUser.requestFocus();
        } else {
            result = true;
        }
        return result;
    }
}