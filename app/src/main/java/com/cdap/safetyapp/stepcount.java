package com.cdap.safetyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cdap.safetyapp.viewmodels.MainViewModel;

import net.danlew.android.joda.BuildConfig;
import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

public class stepcount extends AppCompatActivity {

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
                sendNotification("DID Alert", String.format(" Are You in a Trouble Do You Need Help"));


            } else {
                textViewStatus.setText("Not Running");
            }
        });


    }

    private void sendNotification(String title, String content) {

        Toast.makeText(this, "" + content, Toast.LENGTH_SHORT).show();

        String NOTIFICATION_CHANNEL_ID = "DID Safety APP";

        Intent intent = new Intent(this, StepCountCall.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "hulhilli";
            String description = "ykyfkyfkfkf";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent);

            notificationManager.notify(1005, builder.build());
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent);


            notificationManager.notify(1005, builder.build());
        }
    }
}