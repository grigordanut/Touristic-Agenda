package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventImageDeleteEvents extends AppCompatActivity implements EventAdapter.OnItemClickListener {

    private TextView tVDeleteNoEvents, tVDeleteEvents;

    //Retrieve data from Users database
    private DatabaseReference databaseRefUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //Retrieve data from Events database
    private FirebaseStorage firebaseStDeleteEv;
    private DatabaseReference databaseRefDeleteEv;
    private ValueEventListener valueEvListenerDeleteEv;

    //Retrieve data from Events Location database
    private DatabaseReference dbRefEventsLocation;

    private RecyclerView recyclerViewDeleteEv;
    private EventAdapter adapterDeleteEv;

    private List<Events> listDeleteEv;

    private String user_Name = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_image_delete_events);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Delete events page");

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        //retrieve data from Users database
        databaseRefUser = FirebaseDatabase.getInstance().getReference().child("Users");

        //Initialize the database storage events
        firebaseStDeleteEv = FirebaseStorage.getInstance();
        databaseRefDeleteEv = FirebaseDatabase.getInstance().getReference().child("Events");

        dbRefEventsLocation = FirebaseDatabase.getInstance().getReference().child("Locations");

        tVDeleteNoEvents = findViewById(R.id.tvDeleteNoEvents);
        tVDeleteEvents = findViewById(R.id.tvDeleteEvents);

        recyclerViewDeleteEv = findViewById(R.id.recyclerViewDeleteEvents);
        recyclerViewDeleteEv.setHasFixedSize(true);
        recyclerViewDeleteEv.setLayoutManager(new LinearLayoutManager(this));
        listDeleteEv = new ArrayList<>();

        adapterDeleteEv = new EventAdapter(EventImageDeleteEvents.this, listDeleteEv);
        recyclerViewDeleteEv.setAdapter(adapterDeleteEv);
        adapterDeleteEv.setOnItmClickListener(EventImageDeleteEvents.this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        loadUserDetails();
        loadEvents();
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
                Toast.makeText(EventImageDeleteEvents.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEvents() {

        //retrieve data from firebase database
        valueEvListenerDeleteEv = databaseRefDeleteEv.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listDeleteEv.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Events events = postSnapshot.getValue(Events.class);
                        assert events != null;
                        if (events.getUser_Key().equals(currentUser.getUid())) {
                            events.setEvent_Key(postSnapshot.getKey());
                            listDeleteEv.add(events);
                            tVDeleteNoEvents.setText(listDeleteEv.size() + " events added by: " + user_Name);
                            tVDeleteEvents.setText("Select the event:");
                        }

                        if (listDeleteEv.size() == 0) {
                            tVDeleteNoEvents.setText("No events added by: " + user_Name);
                            tVDeleteEvents.setVisibility(View.INVISIBLE);
                        }
                    }

                    adapterDeleteEv.notifyDataSetChanged();
                } else {
                    tVDeleteNoEvents.setText("No events registered!!");
                    tVDeleteEvents.setVisibility(View.INVISIBLE);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventImageDeleteEvents.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Action of the events onClick
    @SuppressLint("SetTextI18n")
    @Override
    public void onItemClick(int position) {

        Events selected_event = listDeleteEv.get(position);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EventImageDeleteEvents.this);
        alertDialogBuilder
                .setTitle("You selected the event:\n" + selected_event.getEvent_Name() + "!!")
                .setMessage("Are sure to delete this event?")
                .setCancelable(true)
                .setPositiveButton("YES", (dialog, id) -> {
                    Events selected_Event = listDeleteEv.get(position);

                    final String event_Key = selected_Event.getEvent_Key();

                    final String location_Key = selected_Event.getEventLocationKey();

                    StorageReference imageReference = firebaseStDeleteEv.getReferenceFromUrl(selected_Event.getEvent_Image());
                    imageReference.delete().addOnSuccessListener(aVoid -> {
                        databaseRefDeleteEv.child(event_Key).removeValue();
                        dbRefEventsLocation.child(location_Key).removeValue();

                        LayoutInflater inflater = getLayoutInflater();
                        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                        TextView text = layout.findViewById(R.id.tvToast);
                        ImageView imageView = layout.findViewById(R.id.imgToast);
                        text.setText("The event was successfully deleted!!");
                        imageView.setImageResource(R.drawable.baseline_delete_forever_24);
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    });
                })

                .setNegativeButton("NO", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseRefDeleteEv.removeEventListener(valueEvListenerDeleteEv);
    }
}