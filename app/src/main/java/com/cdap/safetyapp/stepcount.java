package com.cdap.safetyapp;

import androidx.annotation.NonNull;
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
import android.view.View;
import android.widget.Button;
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

public class stepcount extends AppCompatActivity {

    Button btnnotification;

    private static  int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    private static  int SEND_SMS_PERMISSION_REQUEST_CODE = 1;

    private  FirebaseFirestore db = FirebaseFirestore.getInstance();

    private  FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private  DocumentReference documentReferenceUser=db.collection("users")
            .document(firebaseAuth.getCurrentUser().getUid());

    private static final String CHANNEL_ID = "chnnel1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stepcount);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        JodaTimeAndroid.init(this);

        TextView textViewCount = findViewById(R.id.count);
        TextView textViewAvg = findViewById(R.id.avg);
        TextView textViewStatus = findViewById(R.id.status);

        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.getStepCount().observe(this, count -> {
            textViewCount.setText(String.valueOf(count));
        });

        mainViewModel.getAverageLiveData().observe(this, aDouble -> {
            textViewAvg.setText(String.valueOf(aDouble));
        });

        mainViewModel.getIsRunning().observe(this, aBoolean -> {
            if (aBoolean) {
                textViewStatus.setText("Running");
//                notfy();
            sendNotification("DID Alert",String.format(" Are You in a Trouble Do You Need Help"));


            } else {
                textViewStatus.setText("Not Running");
            }
        });


    }
    private void sendNotification(String title, String content) {

        Toast.makeText(this, ""+content, Toast.LENGTH_SHORT).show();
        String NOTIFICATION_CHANNEL_ID ="DID Safety APP";
        NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(stepcount.this, soscall.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);




        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            //config

            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentTitle(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent);



        Notification notification= builder.build();
        notificationManager.notify(new Random().nextInt(),notification);
        




    }

//    public void notfy(){
//        btnnotification.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String message="Are You in Dangerous are";
//                NotificationCompat.Builder builder=new NotificationCompat.Builder(stepcount.this
//                )
//                        .setSmallIcon(R.drawable.ic_safety)
//                        .setContentTitle("New Notification")
//                        .setContentText(message)
//                        .setAutoCancel(true);
//
//                Intent intent=new Intent(stepcount.this,MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra("message",message);
//                PendingIntent pendingIntent=PendingIntent.getActivity(stepcount.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//                builder.setContentIntent(pendingIntent);
//
//                NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE
//
//                );
//                notificationManager.notify(0,builder.build());
//
//            }
//        });
//    }

    }