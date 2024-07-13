package com.mishraaditya.mychats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mishraaditya.mychats.databinding.ActivityOtpBinding;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    ActivityOtpBinding binding;
    String verificationId;
    FirebaseAuth firebaseAuth;
    ImageView imageView;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding= ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView=findViewById(R.id.imageView);
        Animate(imageView);



        phoneNumber= getIntent().getStringExtra("phoneNumber");
        binding.phoneLbl.setText("Verify: "+ phoneNumber);

        firebaseAuth=FirebaseAuth.getInstance();

        PhoneAuthOptions options=new PhoneAuthOptions.Builder(firebaseAuth).
                setPhoneNumber(phoneNumber).
                setActivity(OtpActivity.this).setTimeout(60L,TimeUnit.SECONDS)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }


                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        verificationId=verifyId;
                    }

                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,binding.otpView.getText().toString());

                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(OtpActivity.this,"Logged In Successfully",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(OtpActivity.this,SetupProfileActivity.class));
                            finishAffinity();//will finish all activity
                        }else{
                            Toast.makeText(OtpActivity.this,"Failed: TryAgain",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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