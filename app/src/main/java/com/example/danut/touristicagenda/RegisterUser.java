package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterUser extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private TextInputEditText firstNameReg;
    private TextInputEditText lastNameReg;
    private TextInputEditText emailReg;
    private TextInputEditText passReg;
    private TextInputEditText confPassReg;
    private String firstName_reg, lastName_reg, email_reg, pass_reg, confPass_reg;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Register Users");

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        firstNameReg = findViewById(R.id.etFirstNameReg);
        lastNameReg = findViewById(R.id.etLastNameReg);
        emailReg = findViewById(R.id.etEmailReg);
        passReg = findViewById(R.id.etPassReg);
        confPassReg = findViewById(R.id.etConfPassReg);

        //Upload data to Users database
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        Button btn_logReg = findViewById(R.id.btnLogReg);
        btn_logReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLog = new Intent(RegisterUser.this, LoginUser.class);
                startActivity(intentLog);
                finish();
            }
        });

        Button btn_register = findViewById(R.id.btnRegister);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateUserRegData()) {

                    progressDialog.setMessage("Register Users Details!");
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(email_reg, pass_reg).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                uploadUserData();

                            } else {
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (Exception e) {
                                    Toast.makeText(RegisterUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void uploadUserData() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String user_Id = firebaseUser.getUid();
            Users users_data = new Users(firstName_reg, lastName_reg, email_reg);
            databaseReference.child(user_Id).setValue(users_data).addOnCompleteListener(RegisterUser.this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        firebaseUser.sendEmailVerification();

                        Toast.makeText(RegisterUser.this, "Users Successfully Registered.\nVerification Email has been sent!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e) {
                            Toast.makeText(RegisterUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    progressDialog.dismiss();
                }
            });
        }
    }

    private Boolean validateUserRegData() {
        boolean result = false;

        firstName_reg = Objects.requireNonNull(firstNameReg.getText()).toString().trim();
        lastName_reg = Objects.requireNonNull(lastNameReg.getText()).toString().trim();
        email_reg = Objects.requireNonNull(emailReg.getText()).toString().trim();
        pass_reg = Objects.requireNonNull(passReg.getText()).toString().trim();
        confPass_reg = Objects.requireNonNull(confPassReg.getText()).toString().trim();

        if (firstName_reg.isEmpty()) {
            firstNameReg.setError("First Name can be empty");
            firstNameReg.requestFocus();
        } else if (lastName_reg.isEmpty()) {
            lastNameReg.setError("Last Name cannot be empty");
            lastNameReg.requestFocus();
        } else if (email_reg.isEmpty()) {
            emailReg.setError("Email Address cannot be empty");
            emailReg.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email_reg).matches()) {
            emailReg.setError("Enter a valid Email Address");
            emailReg.requestFocus();
        } else if (pass_reg.isEmpty()) {
            passReg.setError("Password cannot be empty");
            passReg.requestFocus();
        } else if (pass_reg.length() < 6) {
            passReg.setError("Password too short, enter minimum 6 character long");
        } else if (confPass_reg.isEmpty()) {
            confPassReg.setError("Confirm Password cannot be empty");
            confPassReg.requestFocus();
        } else if (!pass_reg.equals(confPass_reg)) {
            confPassReg.setError("The Confirm Password does not match Password");
            confPassReg.requestFocus();
        } else {
            result = true;
        }
        return result;
    }
}