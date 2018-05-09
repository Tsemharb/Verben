package com.example.art.verben;

import java.util.Date;

/**
 * Created by art on 29.04.18.
 */

public class word {

    private int id;
    private int array_ind;
    private String de;
    private String ru;
    private int status;
    private long timing;
    private int right_count;
    private int wrong_count;

    public word (int id, String de, String ru, int status, long timing, int right_count, int wrong_count){
        this.id = id;
        this.de = de;
        this.ru = ru;
        this.status = status;
        this.timing = timing;
        this.right_count = right_count;
        this.wrong_count = wrong_count;
    }

    public int get_id(){
        return id;
    }

    public String get_question(int question_mode){
        if (question_mode==1)
            return de;
        else return ru;
    }

    public String get_answer(int question_mode){
        if (question_mode==1)
            return ru;
        else return de;
    }

    public String get_de(){
        return de;
    }

    public String get_ru(){
        return ru;
    }

    public int get_status(){
        return status;
    }

    public int get_right_count(){
        return right_count;
    }

    public int get_wrong_count(){
        return wrong_count;
    }

    public long get_timing(){
        return timing;
    }

    public void set_array_ind(int ind){
        this.array_ind = ind;
    }

    public void set_timing(){
        Date day = new Date();
        long mills = day.getTime();
        timing = mills;
    }

    public void increment_status(){
        status++;
    }

    public void increment_right_count(){
        right_count++;
    }

    public void increment_wrong_count(){
        wrong_count++;
    }

    public int get_array_ind(){
        return array_ind;
    }

    public void status_to_zero(){
        status = 0;
    }
}
