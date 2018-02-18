package com.companyname.facebook_live;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button loginB, otpB, reset;
    EditText mobileEdit, otpEdit;
    String mobile, verification;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callBack;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        loginB = findViewById(R.id.loginButton);
        otpB = findViewById(R.id.otpButton);
        mobileEdit = findViewById(R.id.mobileNumber);
        otpEdit = findViewById(R.id.otp);
        reset = findViewById(R.id.resetButton);

        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mobileEdit.getText().toString()).length() < 8) {
                    Toast.makeText(MainActivity.this, "Invalid Mobile NUMBER", Toast.LENGTH_SHORT).show();
                } else {
                    sendOTP();
                }
            }
        });

        otpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEdit.getText().toString();
                if (otp.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter OTP First", Toast.LENGTH_SHORT).show();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, otp);
                    verify(credential);
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMobile();
            }
        });
        callBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                verify(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MainActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Toast.makeText(MainActivity.this, "OTP SENT", Toast.LENGTH_SHORT).show();
                verification = verificationId;
                mobileEdit.setEnabled(false);
                loginB.setEnabled(false);
                reset.setVisibility(View.VISIBLE);
                otpB.setVisibility(View.VISIBLE);
                otpEdit.setVisibility(View.VISIBLE);
            }
        };
    }

    private void sendOTP() {
        mobile = mobileEdit.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                MainActivity.this,
                callBack
        );
    }

    private void changeMobile() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mobileEdit.setEnabled(true);
                        loginB.setEnabled(true);
                        reset.setVisibility(View.GONE);
                        otpB.setVisibility(View.GONE);
                        otpEdit.setVisibility(View.GONE);
                        mobileEdit.setText("");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Edit mobile Number ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void verify(PhoneAuthCredential phoneAuthCredential) {
        mFirebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(MainActivity.this, Profile.class);
                            startActivity(i);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(MainActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
