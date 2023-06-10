package com.example.danut.touristicagenda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class ResetPassword extends AppCompatActivity {

    //declare variables
    private FirebaseAuth firebaseAuth;
    private TextInputEditText emailResetPass;
    private String email_ResetPass;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Reset Password");

        progressDialog = new ProgressDialog(this);

        //initialize variables
        emailResetPass = findViewById(R.id.etEmailResetPass);

        firebaseAuth = FirebaseAuth.getInstance();

        //Action of the button Reset password
        Button btn_ResetPass = findViewById(R.id.btnResetPass);
        btn_ResetPass.setOnClickListener(view -> resetPassword());
    }

    private void resetPassword() {

        if (validateResetPassData()) {

            progressDialog.setMessage("Reset user Password!!");
            progressDialog.show();

            firebaseAuth.sendPasswordResetEmail(email_ResetPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    Toast.makeText(ResetPassword.this, "Password reset email has been sent", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ResetPassword.this, LoginUser.class));
                    finish();

                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthInvalidUserException e) {
                        emailResetPass.setError("This email is not registered");
                        emailResetPass.requestFocus();
                    } catch (Exception e) {
                        Toast.makeText(ResetPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                progressDialog.dismiss();
            });
        }
    }

    private Boolean validateResetPassData() {

        boolean result = false;

        email_ResetPass = emailResetPass.getText().toString().trim();

        //check the input fields
        if (email_ResetPass.isEmpty()) {
            emailResetPass.setError("Enter your Email Address");
            emailResetPass.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_ResetPass).matches()) {
            emailResetPass.setError("Enter a valid Email Address");
            emailResetPass.requestFocus();
        } else {
            result = true;
        }
        return result;
    }
}