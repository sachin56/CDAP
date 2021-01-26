package com.cdap.safetyapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cdap.safetyapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import  androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cdap.safetyapp.models.User;
import com.cdap.safetyapp.viewmodels.MainViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import net.danlew.android.joda.BuildConfig;
import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Random;

import timber.log.Timber;

public class soscall extends AppCompatActivity {
    private static int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    private static int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private DocumentReference documentReferenceUser = db.collection("users")
            .document(firebaseAuth.getCurrentUser().getUid());

    public void sosbutton(){

    ImageButton Image = (ImageButton)findViewById(R.id.imgbtn);

        Image.setOnClickListener(v ->{
        documentReferenceUser.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.e("User", "DocumentSnapshot data: " + document.getData());

                                User user = document.toObject(User.class);

                                String number = user.getTelephoneNumber();
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + number));
                                startActivity(intent);


                                try {
                                    SmsManager sms = SmsManager.getDefault();
                                    sms.sendTextMessage(user.getTelephoneNumber(), null, "\"https://www.google.com/maps/@?api=1&map_action=map&AIzaSyCpSyKWT1z1W0tglZOnGWKc1DfHCJL9ZsM", null, null);
                                } catch (Exception e) {
                                    Toast.makeText(soscall.this, "Sms not Send", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }


                            } else {
                                Log.e("User", "No such document");
                            }
                        } else {
                            Log.e("User", "get failed with ", task.getException());
                            Toast.makeText(soscall.this, "No Internet Connection. Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    });
    }
    private boolean checkCallPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkSmsPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MAKE_CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission granted for making calls", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission granted for send sms", Toast.LENGTH_SHORT).show();
            }
        }

    }

}

