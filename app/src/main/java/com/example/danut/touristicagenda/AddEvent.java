package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AddEvent extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ValueEventListener eventListenerUser;

    private TextView tVEventUserName;
    private TextView tVEventUserKey;

    private String user_Key;

    Users users_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //retrieve data from database into text views
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        tVEventUserName = findViewById(R.id.tvEventUserName);
        tVEventUserKey = findViewById(R.id.tvEventUserKey);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            user_Key = bundle.getString("USERKey");
        }
    }

    public void onStart() {
        super.onStart();
        loadUserData();
    }

    public void loadUserData() {

        eventListenerUser = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NewApi"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve data from database
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    users_data = postSnapshot.getValue(Users.class);

                    assert users_data != null;
                    if (Objects.requireNonNull(postSnapshot.getKey()).equals(user_Key)) {
                        tVEventUserName.setText("Add Event to:  " + users_data.getUser_firstName() + " " + users_data.getUser_lastName());
                        tVEventUserKey.setText(user_Key);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddEvent.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}