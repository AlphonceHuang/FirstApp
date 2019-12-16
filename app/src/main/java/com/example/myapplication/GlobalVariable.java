package com.example.myapplication;

import android.app.Application;

public class GlobalVariable extends Application {
    private double person_height, person_weight, person_BMI;

    public void setPerson_height(double person_height) {
        this.person_height = person_height;
    }
    public void setPerson_weight(double person_weight){
        this.person_weight = person_weight;
    }
    public void setPerson_BMI(double person_BMI){
        this.person_BMI = person_BMI;
    }
    public double getPerson_height(){
        return this.person_height;
    }
    public double getPerson_weight(){
        return this.person_weight;
    }
    public double getPerson_BMI(){
        return this.person_BMI;
    }
}
