package com.example.danut.touristicagenda;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
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

    //Retrieve data from Events database
    private FirebaseStorage firebaseStEvents;
    private DatabaseReference databaseRefEvents;
    private ValueEventListener valueEvListenerEvents;

    //Add Events updated data
    private StorageReference storageRefEventUp;
    private DatabaseReference databaseRefEventUp;
    private StorageTask uploadTaskEventUp;

    private ImageView ivEventUp;
    private Uri imageUriUp;

    private TextView tVEventUp;
    private TextInputEditText etDateEventUp, etNameEventUp, etAddressEventUp;
    private EditText etMessageEventUp;

    private String etEvent_DateUp, etEvent_NameUp, etEvent_AddressUp, etEvent_MessageUp;

    //Data received from Events image
    private String event_DateUp = "";
    private String event_NameUp = "";
    private String event_AddressUp = "";
    private String event_MessageUp = "";
    private String event_ImageUp = "";
    private String event_KeyUp = "";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        storageRefEventUp = FirebaseStorage.getInstance().getReference("Events");
        databaseRefEventUp = FirebaseDatabase.getInstance().getReference("Events");

        //initialise variables
        tVEventUp = findViewById(R.id.tvEventUpdate);

        etDateEventUp = findViewById(R.id.etDateEventUpdate);
        etDateEventUp.setEnabled(false);
        etNameEventUp = findViewById(R.id.etNameEventUpdate);
        etAddressEventUp = findViewById(R.id.etAddressEventUpdate);
        etMessageEventUp = findViewById(R.id.etMessageEventUpdate);
        ivEventUp = findViewById(R.id.imgViewEventUpdate);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            event_DateUp = bundle.getString("EDate");
            event_NameUp = bundle.getString("EName");
            event_AddressUp = bundle.getString("EAddress");
            event_MessageUp = bundle.getString("EMessage");
            event_ImageUp = bundle.getString("EImage");
            event_KeyUp = bundle.getString("EKey");
        }

        etDateEventUp.setText(event_DateUp);
        etNameEventUp.setText(event_NameUp);
        etAddressEventUp.setText(event_AddressUp);
        etMessageEventUp.setText(event_MessageUp);

        ImageButton buttonTakePictureUp = findViewById(R.id.btnTakePictureEventUpdate);
        Button buttonSaveEventUp = findViewById(R.id.btnSaveEventUpdate);

        Picasso.get()
                .load(event_ImageUp)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(ivEventUp);

        ivEventUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        //take a picture by camera
        buttonTakePictureUp.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, TAKE_PICTURE);
                    } else {
                        openCamera();
                    }
                }
                else {
                    openCamera();
                }
            }
        });

        //Action button Save Event
        buttonSaveEventUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploadTaskEventUp != null && uploadTaskEventUp.isInProgress()) {
                    Toast.makeText(UpdateEvent.this, "Update Event in progress", Toast.LENGTH_SHORT).show();
                } else {
                    if (imageUriUp == null) {
                        alertDialogBikePicture();
                    } else {
                        updateEventWithNewPicture();
                    }
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
        switch (requestCode) {
            case PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    openCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    // permission deniedDisable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PICTURE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    imageUriUp = data.getData();
                    ivEventUp.setImageURI(imageUriUp);
                    Toast.makeText(getApplicationContext(), "Image picked from Gallery", Toast.LENGTH_SHORT).show();
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

    private void deleteOldEventPicture() {
        progressDialog.show();

        StorageReference storageRefDelete = getInstance().getReferenceFromUrl(event_ImageUp);
        storageRefDelete.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(UpdateEvent.this, "Previous image deleted", Toast.LENGTH_SHORT).show();
                ivEventUp.setImageResource(R.drawable.image_event);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    //Upload the updated Event into the Events table
    public void updateEventWithNewPicture() {

        if (validateUpdateEventDetails()) {
            etEvent_DateUp = Objects.requireNonNull(etDateEventUp.getText()).toString().trim();
            etEvent_NameUp = Objects.requireNonNull(etNameEventUp.getText()).toString().trim();
            etEvent_AddressUp = Objects.requireNonNull(etAddressEventUp.getText()).toString().trim();
            etEvent_MessageUp = etMessageEventUp.getText().toString().trim();

            progressDialog.setTitle("The Event is updating!");
            progressDialog.show();

            final StorageReference fileReference = storageRefEventUp.child(System.currentTimeMillis() + "." + getFileExtension(imageUriUp));
            uploadTaskEventUp = fileReference.putFile(imageUriUp)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(@NonNull final Uri uri) {

                                    databaseRefEventUp.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                                String event_Key = postSnapshot.getKey();
                                                assert event_Key != null;

                                                if (event_Key.equals(event_KeyUp)) {
                                                    postSnapshot.getRef().child("event_Date").setValue(etEvent_DateUp);
                                                    postSnapshot.getRef().child("event_Name").setValue(etEvent_NameUp);
                                                    postSnapshot.getRef().child("event_Address").setValue(etEvent_AddressUp);
                                                    postSnapshot.getRef().child("event_Message").setValue(etEvent_MessageUp);
                                                    postSnapshot.getRef().child("event_Image").setValue(uri.toString());
                                                    deleteOldEventPicture();
                                                }
                                            }

                                            Toast.makeText(UpdateEvent.this, "The Event will be updated", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(UpdateEvent.this, UserPage.class));
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(UpdateEvent.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateEvent.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            //show upload Progress
                            double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Updated: " + (int) progress + "%");
                            progressDialog.setProgress((int) progress);
                        }
                    });
        }
    }

    private void uploadEventWithOldPicture() {

        if (validateUpdateEventDetails()) {

            //Add a new Event into the Event's table
            etEvent_DateUp = Objects.requireNonNull(etDateEventUp.getText()).toString().trim();
            etEvent_NameUp = Objects.requireNonNull(etNameEventUp.getText()).toString().trim();
            etEvent_AddressUp = Objects.requireNonNull(etAddressEventUp.getText()).toString().trim();
            etEvent_MessageUp = etMessageEventUp.getText().toString().trim();

            progressDialog.setTitle("The Event is updating!");
            progressDialog.show();

            databaseRefEventUp.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        String event_Key = postSnapshot.getKey();
                        assert event_Key != null;

                        if (event_Key.equals(event_KeyUp)) {
                            postSnapshot.getRef().child("event_Date").setValue(etEvent_DateUp);
                            postSnapshot.getRef().child("event_Name").setValue(etEvent_NameUp);
                            postSnapshot.getRef().child("event_Address").setValue(etEvent_AddressUp);
                            postSnapshot.getRef().child("event_Message").setValue(etEvent_MessageUp);
                        }
                    }

                    Toast.makeText(UpdateEvent.this, "The Event will be updated", Toast.LENGTH_SHORT).show();
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
        etEvent_NameUp = Objects.requireNonNull(etNameEventUp.getText()).toString().trim();
        etEvent_AddressUp = Objects.requireNonNull(etAddressEventUp.getText()).toString().trim();
        etEvent_MessageUp = etMessageEventUp.getText().toString().trim();

        if (TextUtils.isEmpty(etEvent_NameUp)) {
            etNameEventUp.setError("Please add the name of Event");
            etDateEventUp.requestFocus();
        } else if (TextUtils.isEmpty(etEvent_AddressUp)) {
            etAddressEventUp.setError("Please add the address of Event");
            etAddressEventUp.requestFocus();
        } else if (TextUtils.isEmpty(etEvent_MessageUp)) {
            etMessageEventUp.setError("Please add the message of Event");
            etMessageEventUp.requestFocus();
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
                            tVEventUp.setText(String.format("Update %s", user.getUser_firstName() + " " + user.getUser_lastName() + "'s events"));
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

    public void alertDialogBikePicture() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("No Event picture changed.")
                .setMessage("Update the Event with old picture.")
                .setPositiveButton("YES",
                        (arg0, arg1) -> uploadEventWithOldPicture())
                .setNegativeButton("CANCEL", (dialog, i) -> dialog.dismiss());

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
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