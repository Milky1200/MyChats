package com.mishraaditya.mychats.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.mishraaditya.mychats.R;
import com.mishraaditya.mychats.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    ImageView imageView;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView=findViewById(R.id.imageView);
        Animate(imageView);
        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            startActivity(new Intent(PhoneNumberActivity.this,MainActivity.class));
            Toast.makeText(PhoneNumberActivity.this,"Logged In",Toast.LENGTH_SHORT).show();
            finish();
        }
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iNext=new Intent(PhoneNumberActivity.this,OtpActivity.class);
                iNext.putExtra("phoneNumber",binding.phoneBox.getText().toString());
                startActivity(iNext);
            }
        });
    }

    private void Animate(ImageView imageView) {
        Glide.with(this)
                .asGif()
                .load(R.drawable.mobile_auth)
                .into(imageView);
    }
}