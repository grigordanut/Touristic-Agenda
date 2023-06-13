package com.example.danut.touristicagenda;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class EventImageShowEvents extends AppCompatActivity implements EventAdapter.OnItemClickListener {

    private TextView tVShowEvents;

    //Retrieve data from Users database
    private DatabaseReference databaseRefUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //Retrieve data from Events database
    private FirebaseStorage firebaseStShowEv;
    private DatabaseReference databaseRefShowEv;
    private ValueEventListener valueEvListenerShowEv;

    //Retrieve data from Events Location database
    private DatabaseReference dbRefEventsLocation;

    private RecyclerView recyclerViewShowEv;
    private EventAdapter adapterShowEv;

    private List<Events> listShowEv;

    private String user_Name = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_image_show_events);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Show events page");

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        //retrieve data from Users database
        databaseRefUser = FirebaseDatabase.getInstance().getReference().child("Users");

        //Initialize the database storage events
        firebaseStShowEv = FirebaseStorage.getInstance();
        databaseRefShowEv = FirebaseDatabase.getInstance().getReference().child("Events");

        dbRefEventsLocation = FirebaseDatabase.getInstance().getReference().child("Locations");

        tVShowEvents = findViewById(R.id.tvShowEvents);

        recyclerViewShowEv = findViewById(R.id.recyclerViewShowEvents);
        recyclerViewShowEv.setHasFixedSize(true);
        recyclerViewShowEv.setLayoutManager(new LinearLayoutManager(this));
        listShowEv = new ArrayList<>();

        adapterShowEv = new EventAdapter(EventImageShowEvents.this, listShowEv);
        recyclerViewShowEv.setAdapter(adapterShowEv);
        adapterShowEv.setOnItmClickListener(EventImageShowEvents.this);
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
                Toast.makeText(EventImageShowEvents.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEvents() {

        //retrieve data from firebase database
        valueEvListenerShowEv = databaseRefShowEv.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listShowEv.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Events events = postSnapshot.getValue(Events.class);
                        assert events != null;
                        if (events.getUser_Key().equals(currentUser.getUid())) {
                            events.setEvent_Key(postSnapshot.getKey());
                            listShowEv.add(events);
                            tVShowEvents.setText(listShowEv.size() + " events added by: " + user_Name);
                        }

                        if (listShowEv.size() == 0) {
                            tVShowEvents.setText("No events added by: " + user_Name);
                        }
                    }

                    adapterShowEv.notifyDataSetChanged();
                } else {
                    tVShowEvents.setText("No events registered!!");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventImageShowEvents.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Action of the events onClick
    @Override
    public void onItemClick(int position) {

        final String[] options = {"Show events in Map!!", "Update this event!!", "Delete this event!!"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, options);
        Events selected_Event = listShowEv.get(position);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setCancelable(false)
                .setTitle("Selected event: " + selected_Event.getEvent_Name() + "!!" + "\nSelect an option:")
                .setAdapter(adapter, (dialog, id) -> {

                    if (id == 0) {
                        startActivity(new Intent(EventImageShowEvents.this, MapsActivity.class));
                    }

                    if (id == 1) {
                        Intent intent = new Intent(EventImageShowEvents.this, UpdateEvent.class);

                        intent.putExtra("EDate", selected_Event.getEvent_Date());
                        intent.putExtra("EName", selected_Event.getEvent_Name());
                        intent.putExtra("EPlace", selected_Event.getEvent_Place());
                        intent.putExtra("EMessage", selected_Event.getEvent_Message());
                        intent.putExtra("EImage", selected_Event.getEvent_Image());
                        intent.putExtra("EKey", selected_Event.getEvent_Key());
                        startActivity(intent);
                    }

                    if (id == 2) {
                        confirmDeletion(position);
                    }
                })
                .setNegativeButton("CLOSE", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Action of the menu Delete and alert dialog
    @SuppressLint("SetTextI18n")
    public void confirmDeletion(int position) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EventImageShowEvents.this);
        alertDialogBuilder
                .setTitle("Delete the event!!")
                .setMessage("Are sure to delete this event?")
                .setCancelable(true)
                .setPositiveButton("YES", (dialog, id) -> {
                    Events selected_Event = listShowEv.get(position);

                    final String event_Key = selected_Event.getEvent_Key();

                    final String location_Key = selected_Event.getEventLocationKey();

                    StorageReference imageReference = firebaseStShowEv.getReferenceFromUrl(selected_Event.getEvent_Image());
                    imageReference.delete().addOnSuccessListener(aVoid -> {
                        databaseRefShowEv.child(event_Key).removeValue();
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
        databaseRefShowEv.removeEventListener(valueEvListenerShowEv);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_image_show_events, menu);
        return true;
    }

    public void menuEventsImageGoBack() {
        startActivity(new Intent(EventImageShowEvents.this, UserPage.class));
        finish();
    }

    public void menuEventsImageAddMenu() {
        startActivity(new Intent(EventImageShowEvents.this, AddEvent.class));
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menuEventsImage_goBack) {
            menuEventsImageGoBack();
        }

        if (item.getItemId() == R.id.menuEventsImage_addMenu) {
            menuEventsImageAddMenu();
        }

        return super.onOptionsItemSelected(item);
    }
}