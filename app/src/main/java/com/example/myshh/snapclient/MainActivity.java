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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText etMessage;
    EditText etIP;
    ListView listViewMessages;
    ImageView imageView;
    Bitmap bitmap;
    ArrayList<String> listMessages = new ArrayList<>();
    ArrayAdapter listAdapter;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMessage = findViewById(R.id.etMessage);
        etIP = findViewById(R.id.etIP);
        listViewMessages = findViewById(R.id.listView);
        imageView = findViewById(R.id.imageView);
        imageView.setRotation(90);

        //Set listview adapter
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listMessages);
        listViewMessages.setAdapter(listAdapter);

        checkPermissions();

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
        new Thread(() -> {
            while (true) {
                //Try to connect, reconnect when connection lost
                try {
                    this.socket = new Socket(etIP.getText().toString(), 50005);
                    StaticSocket.socketToServer = socket;
                    StaticSocket.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //While connected
                while (true) {
                    Julia julia;
                    try {
                        //Read object, new stream for every object, blocks
                        StaticSocket.objectInputStream = new ObjectInputStream(socket.getInputStream());
                        //Blocking read
                        julia = (Julia) StaticSocket.objectInputStream.readObject();
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Try to reconnect if receiving failed
                        break;
                    }
                    System.out.println("Received object");
                    listMessages.add(julia.imie);
                    listMessages.add(julia.nazwisko);
                    listMessages.add(Integer.toString(julia.numer_telefonu));
                    this.bitmap = BitmapFactory.decodeByteArray(julia.imageBytes, 0, julia.imageBytes.length);
                    runOnUiThread(action);
                }
            }
        }).start();
    }

    //Notify adapter
    private Runnable action = new Runnable() {
        @Override
        public void run() {
            listAdapter.notifyDataSetChanged();
            //Set received photo
            imageView.setImageBitmap(bitmap);
        }
    };

    //Send message to server
    public void btnSendClick(View view) {
        new Thread(()->{
            try {
                byte message = Byte.parseByte(etMessage.getText().toString());
                socket.getOutputStream().write(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void btnCameraClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
