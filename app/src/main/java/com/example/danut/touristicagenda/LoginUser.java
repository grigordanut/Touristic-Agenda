package com.example.danut.touristicagenda;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginUser extends AppCompatActivity {

    private TextInputEditText emailLogUser;
    private TextInputEditText passLogUser;

    private String email_logUser, pass_logUser;

    private TextView tvEmailLogUser, tvPassLogUser;

    //private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        emailLogUser = (TextInputEditText) findViewById(R.id.etEmailLog);
        passLogUser = (TextInputEditText) findViewById(R.id.etPassLog);

        //firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        CheckBox rememberCheckBox = findViewById(R.id.rememberCB);

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("remember", "");

        if (checkbox != null) {
            if (checkbox.equals("true")) {
                Intent intent = new Intent(LoginUser.this, UserPage.class);
                startActivity(intent);
            }
            else{
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

        Button buttonCancelLog = findViewById(R.id.btnCancelLog);
        buttonCancelLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailLogUser.setText("");
                passLogUser.setText("");
            }
        });

//        Button buttonRegUser = findViewById(R.id.btnRegUser);
//        buttonRegUser.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent regUser = new Intent(LoginUser.this, RegisterUser.class);
//                startActivity(regUser);
//            }
//        });

        TextView tVForgotPass = findViewById(R.id.tvForgotPass);
        tVForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotPass = new Intent(LoginUser.this, ResetPassword.class);
                startActivity(forgotPass);
            }
        });

//        //log in User
//        Button buttonUserLog = findViewById(R.id.btnUserLog);
//        buttonUserLog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (validateUserLogData()) {
//
//                    progressDialog.setMessage("Login User");
//                    progressDialog.show();
//
//                    firebaseAuth.signInWithEmailAndPassword(email_logUser, pass_logUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                //clear data
//                                emailLogUser.setText("");
//                                passLogUser.setText("");
//                                checkEmailVerification();
//                            } else {
//                                progressDialog.dismiss();
//                                Toast.makeText(LoginUser.this, "Log in failed, you entered a wrong Email or Password", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
//            }
//        });

        tvEmailLogUser = findViewById(R.id.text_dummy_hint_emailLog);
        tvPassLogUser = findViewById(R.id.text_dummy_hint_password);

        //Email Address
        emailLogUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Show white background behind floating Label
                            tvEmailLogUser.setVisibility(View.VISIBLE);
                        }
                    },10);

                }else{
                    //Required to show/hide white background behind floating label during focus change
                    if (Objects.requireNonNull(emailLogUser.getText()).length() > 0)
                        tvEmailLogUser.setVisibility(View.VISIBLE);
                    else
                        tvEmailLogUser.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Password
        passLogUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Show white background behind floating label
                            tvPassLogUser.setVisibility(View.VISIBLE);
                        }
                    }, 10);
                } else {
                    //Required to show/hide white background behind floating label during focus change
                    if (Objects.requireNonNull(passLogUser.getText()).length() > 0)
                        tvPassLogUser.setVisibility(View.VISIBLE);
                    else
                        tvPassLogUser.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

//    private Boolean validateUserLogData() {
//        boolean result = false;
//
//        email_logUser = Objects.requireNonNull(emailLogUser.getText()).toString().trim();
//        pass_logUser = Objects.requireNonNull(passLogUser.getText()).toString().trim();
//
//        if (email_logUser.isEmpty()) {
//            emailLogUser.setError("Enter your Login Email");
//            emailLogUser.requestFocus();
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_logUser).matches()) {
//            Toast.makeText(LoginUser.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
//            emailLogUser.setError("Enter a valid Email Address");
//            emailLogUser.requestFocus();
//        } else if (pass_logUser.isEmpty()) {
//            passLogUser.setError("Enter your Login Password");
//            passLogUser.requestFocus();
//        } else {
//            result = true;
//        }
//        return result;
//    }

//    //check if the email has been verified
//    private void checkEmailVerification() {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (firebaseUser != null) {
//            boolean emailFlag = firebaseUser.isEmailVerified();
//
//            progressDialog.dismiss();
//
//            if (emailFlag) {
//                finish();
//                Toast.makeText(LoginUser.this, "Login successful.", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(LoginUser.this, UserPage.class));
//            } else {
//                Toast.makeText(this, "Please verify your Email first", Toast.LENGTH_SHORT).show();
//                firebaseAuth.signOut();
//            }
//        }
//    }
}