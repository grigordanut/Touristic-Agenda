package com.example.danut.touristicagenda;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
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
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    //Declare Variables
    private static final int PICK_IMAGE = 100;

    private Uri imageUriPicture;
    private String user_ImageId = "";
    private CircleImageView ivAddPicture;

    //Access customer database
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private StorageReference stRefAddUserPicture;
    private DatabaseReference dbRefAddUserPicture;
    private DatabaseReference dbRefUsers;
    private ValueEventListener eventListenerUser;
    private StorageTask eventsUploadTask;

    private TextView tVUserHeader, tVUserPage;

    private ProgressDialog progressDialog;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        Objects.requireNonNull(getSupportActionBar()).setTitle("User main page");

        progressDialog = new ProgressDialog(UserPage.this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        stRefAddUserPicture = FirebaseStorage.getInstance().getReference("Users");
        dbRefAddUserPicture = FirebaseDatabase.getInstance().getReference("Users");

        //retrieve data from database into text views
        dbRefUsers = FirebaseDatabase.getInstance().getReference("Users");

        drawerLayout = findViewById(R.id.activity_user_page);
        navigationView = findViewById(R.id.navViewUserPage);
        View header = navigationView.getHeaderView(0);

        //initialise the variables
        tVUserPage = findViewById(R.id.tvUserPage);
        tVUserHeader = header.findViewById(R.id.tvUserHeader);
        ivAddPicture = header.findViewById(R.id.imgUserPicture);

        findViewById(R.id.layoutAddEvents).setOnClickListener(this);
        findViewById(R.id.layoutShowEvents).setOnClickListener(this);
        findViewById(R.id.layoutUpdateEvents).setOnClickListener(this);
        findViewById(R.id.layoutDeleteEvents).setOnClickListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_userPage, R.string.close_userPage);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        eventListenerUser = dbRefUsers.addValueEventListener(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "NewApi", "NonConstantResourceId"})
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve data from database
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    final Users users_data = postSnapshot.getValue(Users.class);

                    assert users_data != null;
                    assert firebaseUser != null;
                    if (firebaseUser.getUid().equals(postSnapshot.getKey())) {
                        tVUserPage.setText("Welcome: " + users_data.getUser_firstName() + " " + users_data.getUser_lastName());

                        Picasso.get()
                                .load(users_data.getUser_picture())
                                .placeholder(R.mipmap.ic_launcher)
                                .fit()
                                .centerCrop()
                                .into(ivAddPicture);

                        tVUserHeader.setText(users_data.getUser_firstName() + " " + users_data.getUser_lastName());

                        user_ImageId = users_data.getUser_picture();

                        navigationView.setNavigationItemSelectedListener(item -> {
                            int id = item.getItemId();
                            switch (id) {

                                //User add picture
                                case R.id.user_addPicture:
                                    openGallery();
                                    break;

                                //Edit User profile
                                case R.id.user_editProfile:
                                    Intent edit_Profile = new Intent(UserPage.this, UpdateUser.class);
                                    startActivity(edit_Profile);
                                    break;

                                //Change User email
                                case R.id.user_changeEmail:
                                    Intent change_Email = new Intent(UserPage.this, ChangeEmail.class);
                                    startActivity(change_Email);
                                    break;

                                //Change User Password
                                case R.id.user_changePassword:
                                    Intent change_Password = new Intent(UserPage.this, ChangePassword.class);
                                    startActivity(change_Password);
                                    break;

                                default:
                                    return true;
                            }
                            return true;
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserPage.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editProfile() {
        startActivity(new Intent(UserPage.this, UpdateUser.class));
        finish();
    }

    private void changeEmail() {
        startActivity(new Intent(UserPage.this, ChangeEmail.class));
        finish();
    }

    private void changePassword() {
        startActivity(new Intent(UserPage.this, ChangePassword.class));
        finish();
    }

    //user log out
    private void logOutUser() {
        alertDialogUserLogout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.logOutUser) {
            logOutUser();
        }

        if (item.getItemId() == R.id.editProfile) {
            editProfile();
        }

        if (item.getItemId() == R.id.changeEmail) {
            changeEmail();
        }

        if (item.getItemId() == R.id.changePassword) {
            changePassword();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void alertDialogUserLogout() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserPage.this);
        alertDialogBuilder
                .setTitle("User logout!!")
                .setMessage("Are you sure to logout?")
                .setCancelable(false)
                .setPositiveButton("YES", (dialog, id) -> {

                    progressDialog.setTitle("User logout!!");
                    progressDialog.show();

                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();

                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
                    TextView text = layout.findViewById(R.id.tvToast);
                    ImageView imageView = layout.findViewById(R.id.imgToast);
                    text.setText("Logout successful!!");
                    imageView.setImageResource(R.drawable.ic_baseline_logout_24);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();

                    startActivity(new Intent(UserPage.this, LoginUser.class));
                    finish();
                })

                .setNegativeButton("NO", (dialog, id) -> dialog.cancel());

        progressDialog.dismiss();
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventListenerUser != null) {
            dbRefUsers.removeEventListener(eventListenerUser);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //Add Events
            case R.id.layoutAddEvents:
                startActivity(new Intent(UserPage.this, AddEvent.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            //Show Events
            case R.id.layoutShowEvents:
                startActivity(new Intent(UserPage.this, EventImageShowEvents.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            //Update Events
            case R.id.layoutUpdateEvents:
                startActivity(new Intent(UserPage.this, EventImageSelectEvents.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            //Delete Events
            case R.id.layoutDeleteEvents:
                startActivity(new Intent(UserPage.this, EventImageDeleteEvents.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
        }
    }

    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        //gallery.setType("Image/*");
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUriPicture = data.getData();
            ivAddPicture.setImageURI(imageUriPicture);

            checkUserPictureExists();

            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.toast, null);
            TextView text = layout.findViewById(R.id.tvToast);
            ImageView imageView = layout.findViewById(R.id.imgToast);
            text.setText("User picture uploaded!!");
            imageView.setImageResource(R.drawable.baseline_image_24);
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void checkUserPictureExists() {

        if (user_ImageId == null) {
            uploadUserPicture();
        } else {
            uploadUserPicture();
            deleteOldUserPicture();
        }
    }

    //Upload a picture into the Users table
    public void uploadUserPicture() {

        //Add picture into Users database
        progressDialog.setTitle("Uploading user picture!!");
        progressDialog.show();

        final StorageReference fileReference = stRefAddUserPicture.child(System.currentTimeMillis() + "." + getFileExtension(imageUriPicture));
        eventsUploadTask = fileReference.putFile(imageUriPicture)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {

                            dbRefAddUserPicture.addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                        if (firebaseUser.getUid().equals(postSnapshot.getKey())) {
                                            postSnapshot.getRef().child("user_picture").setValue(uri.toString());
                                        }
                                    }

                                    user_ImageId = uri.toString();
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(UserPage.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }))

                .addOnFailureListener(e -> Toast.makeText(UserPage.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnProgressListener(taskSnapshot -> {
                    //show upload Progress
                    double progress = 100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    progressDialog.setProgress((int) progress);
                });
    }

    private void deleteOldUserPicture() {
        StorageReference storageRefDelete = getInstance().getReferenceFromUrl(user_ImageId);
        storageRefDelete.delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(UserPage.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}