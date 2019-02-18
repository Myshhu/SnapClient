package com.example.myshh.snapclient;

import java.io.Serializable;

public class Julia implements Serializable {

    public Julia() {
        this.imie = "Juleczka";
        this.nazwisko = "Sikora";
        this.numer_telefonu = 123456789;
    }

    public Julia(String imie, String nazwisko, int numer_telefonu) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.numer_telefonu = numer_telefonu;
    }

    public String imie;
    public String nazwisko;
    public int numer_telefonu;
    public byte[] imageBytes;
}
