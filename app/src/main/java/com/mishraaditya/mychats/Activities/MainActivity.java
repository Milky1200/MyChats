package com.mishraaditya.mychats.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    users.add(snapshot1.getValue(User.class));
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

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user=snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

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
        TopStatusAdapter topStatusAdapter=new TopStatusAdapter(this,userStatuses);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}