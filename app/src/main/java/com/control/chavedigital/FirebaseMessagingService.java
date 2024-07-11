package com.control.chavedigital;

// Imports

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // If any data in message
        if (message.getData().size() > 0) {
            // If person entered house
            if(Objects.equals(message.getData().get("entered"), "true")) {
                sendNotification(true, message.getData().get("photo"),message.getData().get("name"));
            }
            // If person pressed button
            else {
                sendNotification(false, message.getData().get("photo"), "");
            }
        }
    }

    // Creates and sends notification
    private void sendNotification(boolean entered, String photo, String name) {
        // Creates notification channel, if it doesn't exist
        createNotificationChannel();

        // Creates Home Intent
        Intent intent = new Intent(this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Creates notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(entered ? name + " entrou em sua casa" : "Tem alguém no seu portão")
                .setContentText("Toque para abrir o aplicativo")
                .setAutoCancel(true)
                .setLights(getResources().getColor(R.color.primary),1,1)
                .setContentIntent(pendingIntent);

        // Adds user photo
        Glide.with(this)
            .load(photo)
            .apply(new RequestOptions()
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL))
            .into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    Bitmap img = ((BitmapDrawable)resource).getBitmap();
                    builder.setLargeIcon(img);

                    if(!entered)  {
                        builder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(img)
                            .bigLargeIcon(null)
                        );
                    }

                    // Shows notification
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                }
            });

    }

    // Creates notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Portão";
            String description = "Portão";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Sets msgtoken in Firebase database
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        new FirebaseAction().setUserChild(uid, "msgtoken", token, err -> {});
    }
}
