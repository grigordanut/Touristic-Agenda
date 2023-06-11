package com.example.danut.touristicagenda;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddEvent extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int PICK_PICTURE = 100;
    private static final int TAKE_PICTURE = 101;

    private static final int CAPTURE_CAMERA = 1000;
    private static final int PERMISSION_CAMERA = 1001;

    private static final int PERMISSION_LOCATION = 1002;

    //Add event details to Events database
    private StorageReference storageReferenceEvents;
    private DatabaseReference databaseReferenceEvents;
    private StorageTask eventsUploadTask;

    //Add location Details to Locations Database
    private DatabaseReference databaseReferenceLocation;

    //Retrieve data from Users database
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private DatabaseReference databaseRefUser;
    private ValueEventListener eventListenerUser;

    private TextInputEditText eventDate, eventName, eventLocation;
    private EditText eventMessage;

    private TextView tVAddEvent;

    private ImageView ivAddEvent;
    private Uri imageUri;

    private Button btn_saveLocation, btn_SaveEvent;
    private ImageButton btn_TakePicture;

    private String event_Date, event_Name, event_Location, event_Message;
    private String location;
    private double latitude, longitude;

    private ProgressDialog progressDialog;

    private String user_Id = "";
    private String location_Id = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Objects.requireNonNull(getSupportActionBar()).setTitle("CUSTOMER: Add Event");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AddEvent.this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(AddEvent.this);

        storageReferenceEvents = FirebaseStorage.getInstance().getReference("Events");
        databaseReferenceEvents = FirebaseDatabase.getInstance().getReference("Events");
        databaseReferenceLocation = FirebaseDatabase.getInstance().getReference("Locations");

        location_Id = databaseReferenceLocation.push().getKey();

        tVAddEvent = findViewById(R.id.tvAddEvent);

        eventDate = findViewById(R.id.etEventDate);
        eventDate.setEnabled(false);
        eventName = findViewById(R.id.etEventName);
        eventLocation = findViewById(R.id.etEventLocation);
        eventMessage = findViewById(R.id.etEventMessage);

        btn_TakePicture = findViewById(R.id.btnTakePicture);
        btn_saveLocation = findViewById(R.id.btnSaveLocation);
        btn_SaveEvent = findViewById(R.id.btnSaveEvent);

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String insertDate = localDate.format(formatter);
        eventDate.setText(insertDate);

        ivAddEvent = findViewById(R.id.imgViewAddEvent);

        ivAddEvent.setOnClickListener(view -> openGallery());

        btn_TakePicture.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, TAKE_PICTURE);
            } else {
                openCamera();
            }
        });

        btn_saveLocation.setOnClickListener(view -> getLastLocation());

        btn_SaveEvent.setOnClickListener(view -> {
            if (eventsUploadTask != null && eventsUploadTask.isInProgress()) {
                Toast.makeText(AddEvent.this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadEvent();
            }
        });
    }

    //open the phone Gallery
    private void openGallery() {
        Intent pick_Photo = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(pick_Photo, PICK_PICTURE);
    }

    //open camera from the phone
    public void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAPTURE_CAMERA);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PICTURE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    imageUri = data.getData();
                    ivAddEvent.setImageURI(imageUri);

                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                    TextView text = layout.findViewById(R.id.tvToast);
                    ImageView imageView = layout.findViewById(R.id.imgToast);
                    text.setText("Image picked from Gallery!!");
                    imageView.setImageResource(R.drawable.baseline_camera_24);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
                break;

            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    ivAddEvent.setImageURI(imageUri);
                    Toast.makeText(getApplicationContext(), "Image captured by camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Upload a new Events into the Events table
    @SuppressLint("SetTextI18n")
    public void uploadEvent() {

        if (validateEventDetails()) {

            //Read entered Event data
            event_Date = Objects.requireNonNull(eventDate.getText()).toString().trim();

            progressDialog.setTitle("Uploading event details!!");
            progressDialog.show();

            final StorageReference fileReference = storageReferenceEvents.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            eventsUploadTask = fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {

                                String events_Id = databaseReferenceEvents.push().getKey();

                                Events events_Data = new Events(event_Date, event_Name, event_Location, event_Message,
                                        uri.toString(), user_Id, location_Id);

                                assert events_Id != null;
                                databaseReferenceEvents.child(events_Id).setValue(events_Data);

                                saveLocationEvent();

                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                                TextView text = layout.findViewById(R.id.tvToast);
                                ImageView imageView = layout.findViewById(R.id.imgToast);
                                text.setText("The event was successfully uploaded!!");
                                imageView.setImageResource(R.drawable.baseline_event_available_24);
                                Toast toast = new Toast(getApplicationContext());
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.setView(layout);
                                toast.show();

                                startActivity(new Intent(AddEvent.this, UserPage.class));
                                finish();
                            }))

                    .addOnFailureListener(e -> Toast.makeText(AddEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(taskSnapshot -> {
                        //show upload progress
                        double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded: " + (int) progress + "%");
                        progressDialog.setProgress((int) progress);
                    });
        }
    }

    private Boolean validateEventDetails() {
        boolean result = false;

        event_Name = Objects.requireNonNull(eventName.getText()).toString().trim();
        event_Location = Objects.requireNonNull(eventLocation.getText()).toString().trim();
        event_Message = eventMessage.getText().toString().trim();

        if (imageUri == null) {
            Toast.makeText(AddEvent.this, "Please add a  picture", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(event_Name)) {
            eventName.setError("Please add the Name of Event");
            eventName.requestFocus();
        } else if (TextUtils.isEmpty(event_Location)) {
            eventLocation.setError("Please add the Event Address");
            eventLocation.requestFocus();
        } else if (TextUtils.isEmpty(event_Message)) {
            eventMessage.setError("Please add the Event Message ");
            eventMessage.requestFocus();
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUserDetails();
    }

    public void loadUserDetails() {

        //retrieve data from Users database
        databaseRefUser = FirebaseDatabase.getInstance().getReference("Users");

        eventListenerUser = databaseRefUser.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NewApi"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    Users user = postSnapshot.getValue(Users.class);

                    assert user != null;
                    if (firebaseUser.getUid().equals(postSnapshot.getKey())) {
                        tVAddEvent.setText(String.format("Add event to: %s", user.getUser_firstName() + " " + user.getUser_lastName()));
                        user_Id = firebaseUser.getUid();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddEvent.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getLastLocation() {

        Context context = AddEvent.this;
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.activity_save_location, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        final TextInputEditText eTLatitude = promptsView.findViewById(R.id.etLatitude);
        final TextInputEditText eTLongitude = promptsView.findViewById(R.id.etLongitude);
        final TextInputEditText eTAddress = promptsView.findViewById(R.id.etAddress);
        final TextInputEditText eTCityLocation = promptsView.findViewById(R.id.etCityLocation);

        if (ContextCompat.checkSelfPermission(AddEvent.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(AddEvent.this, Locale.getDefault());

                        List<Address> addresses = null;

                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                            eTLatitude.setText(String.valueOf(addresses.get(0).getLatitude()));
                            eTLongitude.setText(String.valueOf(addresses.get(0).getLongitude()));
                            eTAddress.setText(addresses.get(0).getAddressLine(0));
                            eTCityLocation.setText(addresses.get(0).getLocality());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            askPermission();
        }

        // set dialog message
        alertDialogBuilder
                .setTitle("Current Location")
                .setView(promptsView)
                .setCancelable(false)
                .setPositiveButton("Save Location", (dialogInterface, i) -> {
                    latitude = Double.parseDouble(Objects.requireNonNull(eTLatitude.getText()).toString().trim());
                    longitude = Double.parseDouble(Objects.requireNonNull(eTLongitude.getText()).toString().trim());
                    location = Objects.requireNonNull(eTCityLocation.getText()).toString().trim();
                    eventLocation.setText(location);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void saveLocationEvent() {

        EventLocation event_Location = new EventLocation(latitude, longitude, location);
        assert location_Id != null;
        databaseReferenceLocation.child(location_Id).setValue(event_Location).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        LayoutInflater inflater = getLayoutInflater();
                        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                        TextView text = layout.findViewById(R.id.tvToast);
                        ImageView imageView = layout.findViewById(R.id.imgToast);
                        text.setText("Location details have been saved!!");
                        imageView.setImageResource(R.drawable.baseline_location_on_24);
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();

                    } else {
                        Toast.makeText(AddEvent.this, "The Location details has not been saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AddEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    public void askPermission() {
        ActivityCompat.requestPermissions(AddEvent.this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    Toast.makeText(AddEvent.this, "Required Permission", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventListenerUser != null) {
            databaseRefUser.removeEventListener(eventListenerUser);
        }
    }
}