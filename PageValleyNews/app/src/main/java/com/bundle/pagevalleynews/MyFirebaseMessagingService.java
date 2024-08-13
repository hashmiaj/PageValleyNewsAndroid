package com.bundle.pagevalleynews;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.google.firebase.messaging.RemoteMessage;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import okhttp3.Credentials;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        String message = remoteMessage.getData().get("message");
        String data = remoteMessage.getData().get("url");

        showNotification("Page Valley News", message, data);
    }

    public void showNotification(String title, String message, String data) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("DataKey", data);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onNewToken(String token)
    {
        CreateSNSEndPoint(token);
    }

    public void CreateSNSEndPoint(String token)
    {
        //AmazonSNSClient snsClient = new AmazonSNSClient(credentials);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // Context
                "us-east-1:420ae45e-c1ed-44e9-95be-3ca40af00be5", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        AmazonSNSClient snsClient = new AmazonSNSClient();

        try
        {
            snsClient = new AmazonSNSClient(credentialsProvider.getCredentials());
        }catch (Exception ex)
        {
            Log.d("TAG", "Exception Message: "+ ex.getMessage());
        }

        CreatePlatformEndpointRequest endpointRequest = new CreatePlatformEndpointRequest();
        endpointRequest.setPlatformApplicationArn("arn:aws:sns:us-east-1:507887244138:app/GCM/PageValleyNewsAndroid");
        endpointRequest.setToken(token);

        try
        {
            CreatePlatformEndpointResult endpointResult = snsClient.createPlatformEndpoint(endpointRequest);
            String EndpointARN = endpointResult.getEndpointArn();

            SubscribeEndpointToTopic(snsClient, EndpointARN);

        }catch (Exception ex){
            Log.d("TAG", "Exception Message: "+ ex.getMessage());
        }
    }

    public void SubscribeEndpointToTopic(AmazonSNSClient snsClient, String EndpointARN)
    {
        SubscribeRequest subscribeRequest = new SubscribeRequest();
        subscribeRequest.setEndpoint(EndpointARN);
        subscribeRequest.setTopicArn("arn:aws:sns:us-east-1:507887244138:PageValleyNewsTopic");
        subscribeRequest.setProtocol("application");

        SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
    }

}
