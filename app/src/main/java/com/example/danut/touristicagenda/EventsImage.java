package com.example.danut.touristicagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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

public class EventsImage extends AppCompatActivity implements EventsAdapter.OnItemClickListener {

    private TextView tVEvents, tVNoEvents;

    //Retrieve data from Users database
    private DatabaseReference databaseRefUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //Retrieve data from Events database
    private FirebaseStorage firebaseStEvents;
    private DatabaseReference databaseRefEvents;
    private ValueEventListener valueEvListenerEvents;

    //Retrieve data from Events Location database
    private DatabaseReference dbRefEventsLocation;

    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;

    private List<Events> eventsList;

    String user_Name = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_image);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        //retrieve data from Users database
        databaseRefUser = FirebaseDatabase.getInstance().getReference().child("Users");

        //Initialize the database storage events
        firebaseStEvents = FirebaseStorage.getInstance();
        databaseRefEvents = FirebaseDatabase.getInstance().getReference().child("Events");

        dbRefEventsLocation = FirebaseDatabase.getInstance().getReference().child("Locations");

        tVEvents = findViewById(R.id.tvEvents);
        tVNoEvents = findViewById(R.id.tvNumberEvents);

        eventsRecyclerView = findViewById(R.id.evRecyclerView);
        eventsRecyclerView.setHasFixedSize(true);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        eventsList = new ArrayList<>();

        eventsAdapter = new EventsAdapter(EventsImage.this, eventsList);
        eventsRecyclerView.setAdapter(eventsAdapter);
        eventsAdapter.setOnItmClickListener(EventsImage.this);

        Button buttonBackUserPage = findViewById(R.id.btnBackUserPage);
        buttonBackUserPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventsImage.this, UserPage.class));
            }
        });

        Button buttonAddMoreEvents = findViewById(R.id.btnAddMoreEvents);
        buttonAddMoreEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventsImage.this, AddEvent.class));
            }
        });
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
                            tVEvents.setText("List of Events: " + user_Data.getUser_firstName() + " " + user_Data.getUser_lastName());
                            user_Name = user_Data.getUser_firstName() + " " + user_Data.getUser_lastName();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventsImage.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEvents() {

        //retrieve data from firebase database
        valueEvListenerEvents = databaseRefEvents.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    eventsList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Events events = postSnapshot.getValue(Events.class);
                        assert events != null;
                        if (events.getUser_Key().equals(currentUser.getUid())) {
                            events.setEvent_Key(postSnapshot.getKey());
                            eventsList.add(events);
                            tVNoEvents.setText(eventsList.size() + " Events added by: " + user_Name);
                        }
                        else{
                            tVNoEvents.setText("No Events added by: " + user_Name);
                        }

                        progressDialog.dismiss();
                    }

                    eventsAdapter.notifyDataSetChanged();
                }

                else{
                    tVNoEvents.setText("No Events registered");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventsImage.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    //Action of the menu onClick
    @Override
    public void onItemClick(int position) {

        final String[] options = {"Show events in Map", "Update this event", "Delete this Event"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, options);
        Events selected_Event = eventsList.get(position);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setCancelable(false)
                .setTitle("You selected: " + selected_Event.getEvent_Name() + " event" + "\nSelect an option:")
                .setAdapter(adapter, (dialogInterface, i) -> {

                    if (i == 0) {
                        startActivity(new Intent(EventsImage.this, MapsActivity.class));
                    }

                    if (i == 1) {
                        Intent intent = new Intent(EventsImage.this, UpdateEvent.class);

                        Events selected_Event1 = eventsList.get(position);
                        intent.putExtra("EDate", selected_Event1.getEvent_Date());
                        intent.putExtra("EName", selected_Event1.getEvent_Name());
                        intent.putExtra("EAddress", selected_Event1.getEvent_Address());
                        intent.putExtra("EMessage", selected_Event1.getEvent_Message());
                        intent.putExtra("EImage", selected_Event1.getEvent_Image());
                        intent.putExtra("EKey", selected_Event1.getEvent_Key());
                        startActivity(intent);
                    }

                    if (i == 2) {
                        confirmDeletion(position);
                    }
                })
                .setNegativeButton("CLOSE", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Action of the menu Delete and alert dialog
    public void confirmDeletion(int position) {

        AlertDialog.Builder builderAlert = new AlertDialog.Builder(EventsImage.this);
        builderAlert.setMessage("Are sure to delete this item?");
        builderAlert.setCancelable(true);
        builderAlert.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Events selected_Event = eventsList.get(position);

                        final String event_Key = selected_Event.getEvent_Key();

                        final String location_Key = selected_Event.getEventLocationKey();

                        StorageReference imageReference = firebaseStEvents.getReferenceFromUrl(selected_Event.getEvent_Image());
                        imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {
                                databaseRefEvents.child(event_Key).removeValue();
                                dbRefEventsLocation.child(location_Key).removeValue();
                                Toast.makeText(EventsImage.this, "The Event has been deleted successfully ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        builderAlert.setNegativeButton(
                "CANCEL",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builderAlert.create();
        alert11.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseRefEvents.removeEventListener(valueEvListenerEvents);
        tVNoEvents.setText(eventsList.size() + " Events added by " + user_Name);
    }
}