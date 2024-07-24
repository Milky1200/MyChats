package com.mishraaditya.mychats.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageKt;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mishraaditya.mychats.Adaptors.TopStatusAdapter;
import com.mishraaditya.mychats.Models.Status;
import com.mishraaditya.mychats.Models.UserStatus;
import com.mishraaditya.mychats.R;
import com.mishraaditya.mychats.Models.User;
import com.mishraaditya.mychats.Adaptors.UsersAdapter;
import com.mishraaditya.mychats.databinding.ActivityMainBinding;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    UsersAdapter adapter;
    FirebaseDatabase database;

    ArrayList<User> users;
    ActivityMainBinding binding;

    ArrayList<UserStatus> userStatuses;

    ProgressDialog progressDialog;

    TopStatusAdapter topStatusAdapter;

    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading Status...");
        progressDialog.setCancelable(false);

        userStatuses=new ArrayList<UserStatus>();
        setStatusAdapter(userStatuses);
        users=new ArrayList<>();
        database=FirebaseDatabase.getInstance();
        adapter=new UsersAdapter(this,users);
        binding.recyclerView.setAdapter(adapter);
///Setting the chat users
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    User user1=snapshot1.getValue(User.class);
                    if(!user1.getUid().equals(FirebaseAuth.getInstance().getUid()))
                        users.add(user1);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.status){
                    Intent intent=new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,75);

                }
                return false;
            }
        });
///Identifying The main user
        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user=snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//StatusCode
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    userStatuses.clear();

                    for(DataSnapshot storySnapshot : snapshot.getChildren()) {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();

                        for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }

                    topStatusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
///StatusCode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            progressDialog.show();
            if(data.getData()!=null){
                FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
                Date date=new Date();
                StorageReference storageReference=firebaseStorage.getReference()
                .child("status").child(date.getTime()+"");
                storageReference.putFile(data
                        .getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus=new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(date.getTime());

                                    HashMap<String, Object> Obj = new HashMap<>();
                                    Obj.put("name",userStatus.getName());
                                    Obj.put("profileImage",userStatus.getProfileImage());
                                    Obj.put("lastUpdated",userStatus.getLastUpdated());

                                    Status status=new Status(uri.toString(),userStatus.getLastUpdated());
                                    database.getReference().child("stories")
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                            .updateChildren(Obj);
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid())
                                                    .child("statuses").push().setValue(status);

                                    progressDialog.dismiss();


                                }
                            });
                        }
                    }
                });

            }
        }
    }

    private void setStatusAdapter(ArrayList<UserStatus> userStatuses) {
        //to set status
        topStatusAdapter=new TopStatusAdapter(this,userStatuses);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(linearLayoutManager);
        binding.statusList.setAdapter(topStatusAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.search){
            Toast.makeText(this, "Search clicked.", Toast.LENGTH_SHORT).show();
        }else if(item.getItemId()==R.id.settings){
            Toast.makeText(this, "Settings clicked.", Toast.LENGTH_SHORT).show();
        }else if(item.getItemId()==R.id.group){
            Toast.makeText(this, "Group clicked.", Toast.LENGTH_SHORT).show();
        }else if(item.getItemId()==R.id.logOut){
            FirebaseAuth.getInstance().signOut();
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialogue_logout, null);

            // Create the AlertDialog
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            // Find and set up the buttons
            AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnLogout = dialogView.findViewById(R.id.btnLogout);
            TextView tvLogoutMessage = dialogView.findViewById(R.id.tvLogoutMessage);

            btnCancel.setOnClickListener(view -> alertDialog.dismiss());
            btnLogout.setOnClickListener(view -> {
                // Perform the logout action
                Toast.makeText(this, "LogOut.", Toast.LENGTH_SHORT).show();


                finish();
                startActivity(new Intent(MainActivity.this,PhoneNumberActivity.class));

                alertDialog.dismiss();
            });
            // Show the AlertDialog
            alertDialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

}