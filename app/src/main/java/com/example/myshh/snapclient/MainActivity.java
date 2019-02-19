package com.example.myshh.snapclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText etIP;
    Button btnConnect;
    ListView listViewMessages;
    ImageView imageView;
    Bitmap bitmap;
    ArrayList<String> listMessages = new ArrayList<>();
    ArrayAdapter listAdapter;
    Socket socket;
    Thread listeningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIP = findViewById(R.id.etIP);
        btnConnect = findViewById(R.id.btnConnect);
        listViewMessages = findViewById(R.id.listView);
        imageView = findViewById(R.id.imageView);
        imageView.setRotation(90);

        //Set listview adapter
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listMessages);
        listViewMessages.setAdapter(listAdapter);

        checkPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(StaticSocket.socketToServer != null) {
                StaticSocket.socketToServer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnConnectClick(View v) {
        connectAndListenForMessages();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    //Make connection and listen for messages
    private void connectAndListenForMessages() {
        if(listeningThread == null) {
            listeningThread = new Thread(() -> {
               // while (true) {
                    //Try to connect, reconnect when connection lost
                    try {
                        btnConnect.setText("Connecting...");
                        this.socket = new Socket(etIP.getText().toString(), 50005);
                        StaticSocket.socketToServer = socket;
                        StaticSocket.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        btnConnect.setText("Connected");
                    } catch (Exception e) {
                        e.printStackTrace();
                        listeningThread = null;
                        btnConnect.setText("Failed to connect");
                    }

                    while (true) {
                        MessageObject messageObject;
                        try {
                            //Read object, new stream for every object, blocks
                            StaticSocket.objectInputStream = new ObjectInputStream(socket.getInputStream());
                            //Blocking read
                            messageObject = (MessageObject) StaticSocket.objectInputStream.readObject();

                            System.out.println("Received object");
                            listMessages.add("Command: " + Integer.toString(messageObject.command));
                            listMessages.add("Name: " + messageObject.name);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 4;
                            this.bitmap = BitmapFactory.decodeByteArray(messageObject.imageBytes, 0, messageObject.imageBytes.length, options);
                            runOnUiThread(action);
                        } catch (Exception e) {
                            e.printStackTrace();
                            btnConnect.setText("Disconnected");
                            listeningThread = null;
                            //Try to reconnect if receiving failed
                            break;
                        }
                    }
               // }
            });
            listeningThread.start();
        }
    }

    //Notify adapter
    private Runnable action = new Runnable() {
        @Override
        public void run() {
            listAdapter.notifyDataSetChanged();
            //Show received photo
            imageView.setImageBitmap(bitmap);
        }
    };

    public void btnCameraClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
