package com.example.myshh.snapclient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class StaticSocket {
    //Passing socket and streams between activities
    static Socket socketToServer;
    static ObjectOutputStream objectOutputStream;
    static ObjectInputStream objectInputStream;
    static ArrayList<String> listOfClients;
}
