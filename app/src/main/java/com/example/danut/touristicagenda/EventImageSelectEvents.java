package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventImageSelectEvents extends AppCompatActivity implements EventAdapter.OnItemClickListener {

    private TextView tVSelectNoEvents, tVSelectEvents;

    //Retrieve data from Users database
    private DatabaseReference databaseRefUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //Retrieve data from Events database
    private DatabaseReference databaseRefSelectEv;
    private ValueEventListener valueEvListenerSelectEv;

    private RecyclerView recyclerViewSelectEv;
    private EventAdapter adapterSelectEv;

    private List<Events> listSelectEv;

    private String user_Name = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_image_select_events);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Select event to update");

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        //retrieve data from Users database
        databaseRefUser = FirebaseDatabase.getInstance().getReference().child("Users");

        //Initialize the database events
        databaseRefSelectEv = FirebaseDatabase.getInstance().getReference().child("Events");

        tVSelectNoEvents = findViewById(R.id.tvSelectNoEvents);
        tVSelectEvents = findViewById(R.id.tvSelectEvents);

        recyclerViewSelectEv = findViewById(R.id.recyclerViewSelectEvents);
        recyclerViewSelectEv.setHasFixedSize(true);
        recyclerViewSelectEv.setLayoutManager(new LinearLayoutManager(this));

        listSelectEv = new ArrayList<>();

        adapterSelectEv = new EventAdapter(EventImageSelectEvents.this, listSelectEv);
        recyclerViewSelectEv.setAdapter(adapterSelectEv);
        adapterSelectEv.setOnItmClickListener(EventImageSelectEvents.this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        loadUserDetails();
        loadSelectEvents();
    }

    public void loadUserDetails() {

        databaseRefUser.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NewApi"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    FirebaseUser fb_User = firebaseAuth.getCurrentUser();

                    Users user_Data = postSnapshot.getValue(Users.class);
                    if (fb_User != null) {
                        assert user_Data != null;
                        if (fb_User.getUid().equals(postSnapshot.getKey())) {
                            user_Name = user_Data.getUser_firstName() + " " + user_Data.getUser_lastName();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventImageSelectEvents.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSelectEvents() {

        //retrieve data from firebase database
        valueEvListenerSelectEv = databaseRefSelectEv.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listSelectEv.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Events events = postSnapshot.getValue(Events.class);
                        assert events != null;
                        if (events.getUser_Key().equals(currentUser.getUid())) {
                            events.setEvent_Key(postSnapshot.getKey());
                            listSelectEv.add(events);
                            tVSelectNoEvents.setText(listSelectEv.size() + " events added by: " + user_Name);
                            tVSelectEvents.setText("Select the event:");
                        }

                        if (listSelectEv.size() == 0) {
                            tVSelectNoEvents.setText("No events added by: " + user_Name);
                            tVSelectEvents.setVisibility(View.INVISIBLE);
                        }
                    }

                    adapterSelectEv.notifyDataSetChanged();
                } else {
                    tVSelectNoEvents.setText("No events registered!!");
                    tVSelectEvents.setVisibility(View.INVISIBLE);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventImageSelectEvents.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Action of the event onClick
    @Override
    public void onItemClick(int position) {

        Events selected_Events = listSelectEv.get(position);

        Intent intent = new Intent(EventImageSelectEvents.this, UpdateEvent.class);
        intent.putExtra("EDate", selected_Events.getEvent_Date());
        intent.putExtra("EName", selected_Events.getEvent_Name());
        intent.putExtra("EAddress", selected_Events.getEvent_Place());
        intent.putExtra("EMessage", selected_Events.getEvent_Message());
        intent.putExtra("EImage", selected_Events.getEvent_Image());
        intent.putExtra("EKey", selected_Events.getEvent_Key());
        startActivity(intent);
    }
}