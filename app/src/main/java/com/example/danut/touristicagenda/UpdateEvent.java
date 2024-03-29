package com.example.danut.touristicagenda;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UpdateEvent extends AppCompatActivity {

    private static final int PICK_PICTURE = 100;
    private static final int TAKE_PICTURE = 101;

    private static final int CAPTURE_CAMERA = 1000;
    private static final int PERMISSION_CAMERA = 1001;

    //Retrieve data from Users database
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRefUser;
    private ValueEventListener eventEvListenerUsers;

    //Add Events updated data
    private StorageReference storageRefEventUp;
    private DatabaseReference databaseRefEventUp;
    private StorageTask taskEventUpdate;

    private ImageView ivEventUp;
    private Uri imageUriUp;

    private TextView tVEventUp;
    private TextInputEditText eventDateUp, eventNameUp, eventPlaceUp;
    private EditText eventMessageUp;

    private String event_DateUp, event_NameUp, event_PlaceUp, event_MessageUp;

    //Data received from Events image
    private String eventDate_Up = "";
    private String eventName_Up = "";
    private String eventPlace_Up = "";
    private String eventMessage_Up = "";
    private String eventImage_Up = "";
    private String eventKey_Up = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Update the event");

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        storageRefEventUp = FirebaseStorage.getInstance().getReference("Events");
        databaseRefEventUp = FirebaseDatabase.getInstance().getReference("Events");

        //initialise variables
        tVEventUp = findViewById(R.id.tvEventUp);

        eventDateUp = findViewById(R.id.etEventDateUp);
        eventDateUp.setEnabled(false);
        eventNameUp = findViewById(R.id.etEventNameUp);
        eventPlaceUp = findViewById(R.id.etEventPlaceUp);
        eventMessageUp = findViewById(R.id.etEventMessageUp);
        ivEventUp = findViewById(R.id.imgViewEventUp);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            eventDate_Up = bundle.getString("EDate");
            eventName_Up = bundle.getString("EName");
            eventPlace_Up = bundle.getString("EPlace");
            eventMessage_Up = bundle.getString("EMessage");
            eventImage_Up = bundle.getString("EImage");
            eventKey_Up = bundle.getString("EKey");
        }

        eventDateUp.setText(eventDate_Up);
        eventNameUp.setText(eventName_Up);
        eventPlaceUp.setText(eventPlace_Up);
        eventMessageUp.setText(eventMessage_Up);

        ImageButton btn_TakePictureUp = findViewById(R.id.btnTakePictureEventUp);
        Button btn_SaveEventUp = findViewById(R.id.btnSaveEventUp);

        Picasso.get()
                .load(eventImage_Up)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(ivEventUp);

        ivEventUp.setOnClickListener(view -> openGallery());

        //take a picture by camera
        btn_TakePictureUp.setOnClickListener(v -> {
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

        //Action button Save Event
        btn_SaveEventUp.setOnClickListener(view -> {
            if (taskEventUpdate != null && taskEventUpdate.isInProgress()) {
                Toast.makeText(UpdateEvent.this, "Update Event in progress", Toast.LENGTH_SHORT).show();
            } else {
                if (imageUriUp == null) {
                    alertDialogEventPicture();
                } else {
                    updateEventWithNewPicture();
                }
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
        imageUriUp = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriUp);
        startActivityForResult(cameraIntent, CAPTURE_CAMERA);
    }

    //Request the permission to the camera from phone
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                // permission deniedDisable the
                // functionality that depends on this permission.
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PICTURE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    imageUriUp = data.getData();
                    ivEventUp.setImageURI(imageUriUp);

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
                    ivEventUp.setImageURI(imageUriUp);
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

    @SuppressLint("SetTextI18n")
    private void deleteOldEventPicture() {
        StorageReference storageRefDelete = getInstance().getReferenceFromUrl(eventImage_Up);
        storageRefDelete.delete()
                .addOnSuccessListener(aVoid -> {

                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                    TextView text = layout.findViewById(R.id.tvToast);
                    ImageView imageView = layout.findViewById(R.id.imgToast);
                    text.setText("Previous image deleted!!");
                    imageView.setImageResource(R.drawable.baseline_delete_forever_24);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                })
                .addOnFailureListener(e -> Toast.makeText(UpdateEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    //Upload the updated Event into the Events table
    public void updateEventWithNewPicture() {

        if (validateUpdateEventDetails()) {
            event_DateUp = Objects.requireNonNull(eventDateUp.getText()).toString().trim();
            event_NameUp = Objects.requireNonNull(eventNameUp.getText()).toString().trim();
            event_PlaceUp = Objects.requireNonNull(eventPlaceUp.getText()).toString().trim();
            event_MessageUp = eventMessageUp.getText().toString().trim();

            progressDialog.setTitle("Updating event details!!");
            progressDialog.show();

            final StorageReference fileReference = storageRefEventUp.child(System.currentTimeMillis() + "." + getFileExtension(imageUriUp));
            taskEventUpdate = fileReference.putFile(imageUriUp)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            databaseRefEventUp.addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                        String event_Key = postSnapshot.getKey();
                                        assert event_Key != null;

                                        if (event_Key.equals(eventKey_Up)) {
                                            postSnapshot.getRef().child("event_Date").setValue(event_DateUp);
                                            postSnapshot.getRef().child("event_Name").setValue(event_NameUp);
                                            postSnapshot.getRef().child("event_Address").setValue(event_PlaceUp);
                                            postSnapshot.getRef().child("event_Message").setValue(event_MessageUp);
                                            postSnapshot.getRef().child("event_Image").setValue(uri.toString());
                                        }
                                    }

                                    deleteOldEventPicture();

                                    LayoutInflater inflater = getLayoutInflater();
                                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                                    TextView text = layout.findViewById(R.id.tvToast);
                                    ImageView imageView = layout.findViewById(R.id.imgToast);
                                    text.setText("The event has been updated successfully!!");
                                    imageView.setImageResource(R.drawable.baseline_event_available_24);
                                    Toast toast = new Toast(getApplicationContext());
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    toast.setView(layout);
                                    toast.show();

                                    startActivity(new Intent(UpdateEvent.this, UserPage.class));
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(UpdateEvent.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        //show upload Progress
                        double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Updated: " + (int) progress + "%");
                        progressDialog.setProgress((int) progress);
                    });
        }
    }

    private void uploadEventWithOldPicture() {

        if (validateUpdateEventDetails()) {

            //Add a new Event into the Event's table
            event_DateUp = Objects.requireNonNull(eventDateUp.getText()).toString().trim();
            event_NameUp = Objects.requireNonNull(eventNameUp.getText()).toString().trim();
            event_PlaceUp = Objects.requireNonNull(eventPlaceUp.getText()).toString().trim();
            event_MessageUp = eventMessageUp.getText().toString().trim();

            progressDialog.setTitle("Updating event details!!");
            progressDialog.show();

            Query query = databaseRefEventUp.orderByKey().equalTo(eventKey_Up);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        postSnapshot.getRef().child("event_Date").setValue(event_DateUp);
                        postSnapshot.getRef().child("event_Name").setValue(event_NameUp);
                        postSnapshot.getRef().child("event_Place").setValue(event_PlaceUp);
                        postSnapshot.getRef().child("event_Message").setValue(event_MessageUp);
                    }

                    progressDialog.dismiss();

                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                    TextView text = layout.findViewById(R.id.tvToast);
                    ImageView imageView = layout.findViewById(R.id.imgToast);
                    text.setText("The event has been updated successfully!!");
                    imageView.setImageResource(R.drawable.baseline_event_available_24);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();

                    startActivity(new Intent(UpdateEvent.this, UserPage.class));
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UpdateEvent.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean validateUpdateEventDetails() {

        boolean result = false;

        event_NameUp = Objects.requireNonNull(eventNameUp.getText()).toString().trim();
        event_PlaceUp = Objects.requireNonNull(eventPlaceUp.getText()).toString().trim();
        event_MessageUp = eventMessageUp.getText().toString().trim();

        if (TextUtils.isEmpty(event_NameUp)) {
            eventNameUp.setError("Please add the event name");
            eventDateUp.requestFocus();
        } else if (TextUtils.isEmpty(event_PlaceUp)) {
            eventPlaceUp.setError("Please add the event place");
            eventPlaceUp.requestFocus();
        } else if (TextUtils.isEmpty(event_MessageUp)) {
            eventMessageUp.setError("Please add the event message");
            eventMessageUp.requestFocus();
        } else {
            result = true;
        }
        return result;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
        loadUserDetails();
    }

    public void loadUserDetails() {

        //Retrieve data from Users database
        databaseRefUser = FirebaseDatabase.getInstance().getReference("Users");

        eventEvListenerUsers = databaseRefUser.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NewApi"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child_db : dataSnapshot.getChildren()) {
                    final FirebaseUser user_db = firebaseAuth.getCurrentUser();

                    final Users user = child_db.getValue(Users.class);

                    if (user_db != null) {
                        assert user != null;
                        if (user_db.getUid().equals(child_db.getKey())) {
                            tVEventUp.setText(String.format("Update %s", user.getUser_firstName() + " " + user.getUser_lastName() + "'s event"));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateEvent.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void alertDialogEventPicture() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("No event picture changed!!")
                .setMessage("Update the event with the old picture?")
                .setPositiveButton("YES", (dialog, id) -> uploadEventWithOldPicture())
                .setNegativeButton("NO", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventEvListenerUsers != null) {
            databaseRefUser.removeEventListener(eventEvListenerUsers);
        }
    }
}