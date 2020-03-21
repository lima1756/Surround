package com.example.surround.Utils;


import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.util.Log;


public class MyEqualizer {



    int[][] eqModes = { {-100,400,700,1200,800},//FRONT L
            {-100,400,700,1200,800},//FRONT R
            {0,600,1200,600,0},//CENTER
            {1500,900,-300,-1200,-1500},//SUBWOOFER
            {-300,300,900,300,-300},//L
            {-300,300,900,300,-300},//R
            {600,1000,600,0,-600},//BACK L
            {600,1000,600,0,-600}};//BACK R
    float[][] eqVolumes = {
            {1.0f,0.4f},//FRONT L
            {0.4f,1.0f},//FRONT R
            {0.75f,0.75f},//CENTER
            {1.0f,1.0f},//SUBWOOFER
            {1.0f,0.1f},//L
            {0.1f,1.0f},//R
            {1.0f,0.3f},//BACK L
            {0.3f,1.0f}//BACK R
    };



    private Equalizer eq;
    private MediaPlayer mp;

    public MyEqualizer(  MediaPlayer mp){
        this( mp, 0);
    }

    public MyEqualizer(  MediaPlayer mp, int typeOfSpeaker){
        this.mp = mp;
        eq = new Equalizer(0, mp.getAudioSessionId());
        eq.setEnabled(false);
        this.setTypeSpeaker(typeOfSpeaker);
    }

    public void setBandValue(int band, int v){
        v = constrainValue(v);
        int[] newValues = new int[5];
        for(int i=0;i<5;i++){
            if(band == i){
                newValues[i] = v;
            }else{
                newValues[i] = this.eq.getBandLevel((short)band);
            }
        }
        setBandArr(newValues);

    }

    private int constrainValue(int x){
        if (x>1500) return 1500;
        if(x<-1500) return -1500;
        return x;
    }


    public void incrementOrDecreaseValueBand(int band, int increment){
        int newValue = increment+this.eq.getBandLevel((short)band);
        setBandValue(band,newValue);
    }

    public void setTypeSpeaker(int i){
        int[] bandsEq = eqModes[i];
        setBandArr(bandsEq);
        float[] volumeLR = eqVolumes[i];
        mp.setVolume(volumeLR[0], volumeLR[1]);
        //TODO CHECAR SI SE PUEDE ENVIAR TODO EL VOLUMEN A UNA U OTRA BOCINA ( L A AMBOS LADOS, POR EJEMPLO)
        //TODO SI NOS ENVIAN PISTA SEPARADA, NO SERÍA NECESARIO MODIFICAR VOLUMENES, SOLO PORNERLOS AL MAXIMO.


    }
    private void setBandArr( int[] values){
        for(int i=0;i<5;i++){
            this.eq.setBandLevel((short)i,(short)values[i]);
        }
    }

    public Equalizer getEq() {
        return eq;
    }

    public void setEq(Equalizer eq) {
        this.eq = eq;
    }


    public MediaPlayer getMp() {
        return mp;
    }

    public void setMp(MediaPlayer mp) {
        this.mp = mp;
    }
}
