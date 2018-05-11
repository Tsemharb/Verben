package com.example.art.verben;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import java.io.IOException;
import java.util.Date;

public class QueryActivity extends Activity implements View.OnClickListener, View.OnLongClickListener{

    Button btn_hint, btn_still_learning, btn_know;
    TextView tv_question, tv_answer, tv_counter;
    RadioGroup radioGroup;
    DBHelper dbHelper;
    SQLiteDatabase db;
    word[] words =  new word[10];
    ArrayList<word> words_list = new ArrayList<>();
    int current_word_id;
    Timer timer;
    TimerTask mTimerTask;
    Handler h;

//    int id_counter = 1;
    int question = 1; // 1 - de
    boolean end_reached = false;
    boolean finish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_query);

        btn_hint = (Button) findViewById(R.id.btn_hint);
        btn_hint.setOnClickListener(this);
        btn_hint.setOnLongClickListener(this);
        btn_still_learning = (Button)findViewById(R.id.btn_still_learning);
        btn_still_learning.setOnClickListener(this);
        btn_know = (Button)findViewById(R.id.btn_know);
        btn_know.setOnClickListener(this);

        tv_question = (TextView)findViewById(R.id.tv_question);
        tv_answer = (TextView)findViewById(R.id.tv_answer);
        tv_counter = (TextView)findViewById(R.id.tv_counter);
        radioGroup = (RadioGroup)findViewById(R.id.rgroup);

        dbHelper = new DBHelper(this);

        h = new Handler(){
            public void handleMessage(android.os.Message msg){
                btn_know.setEnabled(true);
                show_word();
            }
        };

        try{
            dbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error ("Unable to create DB");
        }

        db = dbHelper.getWritableDatabase();

        for (int i = 0; i<words.length; i++) {
            word transit = get_word(i+1);
            words_list.add(transit);
            if (transit != null){
                words_list.get(i).set_array_ind(i);
                System.out.println(words_list.get(i).get_de());
            }


//            words[i] = get_word(i+1);
//            words[i].set_array_ind(i);
        }

        get_question_mode();
        show_word();
    }

    public void get_question_mode(){
        int selectedRadioButtonID = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedRadioButtonID);
        String selectedRadioButtonText = selectedRadioButton.getText().toString();

        if (selectedRadioButtonText.equals("german"))
            question = 1;
        else question = 0;
    }


    public word get_word(int id_counter){
        String selection = "_id = ?";
        String id_num;
        String[] selectionArgs;
        word a_word = null;
//        int id_counter = 1;

        while(id_counter<=12+1){
            if (id_counter == 12+1){
                end_reached = true;
                a_word = null;
                break;
            }
            id_num = Integer.toString(id_counter);
            selectionArgs = new String[] {id_num};
            Cursor cur = db.query("tab", null, selection, selectionArgs, null, null, null);

            if (cur.moveToFirst()) {
                a_word = new word(cur.getInt(0),
                                    cur.getString(1),
                                    cur.getString(2),
                                    cur.getInt(3),
                                    cur.getLong(4),
                                    cur.getInt(5),
                                    cur.getInt(6));
            }

            if (!is_in_pool(a_word)) {

                if (a_word.get_status() < 5) {
                    id_counter++;
                    break;
                }

                if (a_word.get_status() > 4) {
                    Date day = new Date();
                    long mills_now = day.getTime();
                    long mills_more_than;// = a_word.get_timing() + (a_word.get_status() - 4)*24*60*60*1000;

                    switch (a_word.get_status()) {
                        case 5:
                            mills_more_than = a_word.get_timing() + 60 * 60 * 1000;         //hour
                            break;
                        case 6:
                            mills_more_than = a_word.get_timing() + 6 * 60 * 60 * 1000;       // 6 hours
                            break;
                        case 7:
                            mills_more_than = a_word.get_timing() + 12 * 60 * 60 * 1000;      //12 hours
                            break;
                        case 8:
                            mills_more_than = a_word.get_timing() + 24 * 60 * 60 * 1000;      //24 hours
                            break;
                        case 9:
                            mills_more_than = a_word.get_timing() + 3 * 24 * 60 * 60 * 1000;    //3 days
                            break;
                        case 10:
                            mills_more_than = a_word.get_timing() + 5 * 24 * 60 * 60 * 1000;    //5 days
                            break;
                        default:
                            mills_more_than = a_word.get_timing() + 7 * 24 * 60 * 60 * 1000;    //7 days
                    }

                    if (mills_now > mills_more_than) {
                        id_counter++;
                        break;
                    }
                }
            }
            id_counter++;
        }

        if (a_word == null){
            System.out.println("null");
            return a_word;
        }
        else {
//            System.out.println(a_word.get_id());
            return a_word;
        }

    }

    private boolean is_in_pool(word a_word) {

        for (int i = 0; i<words_list.size(); i++){
            if (words_list.get(i) == null){
                //return false;
                continue;
            }
            if (a_word.get_id() == words_list.get(i).get_id()){
                return true;
            }
        }
        return false;
    }


    public void update_word(int id, long timing, int status, int right, int wrong){
        ContentValues cv = new ContentValues();

        cv.put("status", status);
        cv.put("timing", timing);
        cv.put("right_count", right);
        cv.put("wrong_count", wrong);

        db.update("tab", cv, "_id = ?", new String[] {Integer.toString(id)});
    }


    public void show_word() {

        int randomNum;
        while (true) {
            int index = words_list.size();

            randomNum = ThreadLocalRandom.current().nextInt(0, words_list.size());

            if (words_list.get(randomNum) == null) {
                //go_backward
                int random_num_copy = randomNum;
                while (randomNum >= 0 && words_list.get(randomNum) == null) {
                    randomNum--;
                }
                if (randomNum == -1) {
                    randomNum = random_num_copy;
                }
                while (randomNum < words_list.size() && words_list.get(randomNum) == null) {
                    randomNum = ++random_num_copy;
                }
                if (randomNum == words_list.size()) {
                    randomNum--;
                }
                if (words_list.get(randomNum) == null) {
                    tv_answer.setText("!!!!!!!!!!!!!!");
                    finish = true;
                    break;
                }

            }

//            if (current_word_id != words[randomNum].get_id())               //The same word won't be shown for the second time
            if (current_word_id != words_list.get(randomNum).get_id())               //The same word won't be shown for the second time
                break;
//            else System.out.println("gotcha!");
        }

        if (!finish) {
            //        tv_question.setText(words[randomNum].get_question(question));
            tv_question.setText(words_list.get(randomNum).get_question(question));
            tv_answer.setText("");

            //        if (words[randomNum].get_status() == 1){
            if (words_list.get(randomNum).get_status() == 1) {
                //            tv_counter.setText(Integer.toString(words[randomNum].get_status()) + " correct answer in a row");
                tv_counter.setText(Integer.toString(words_list.get(randomNum).get_status()) + " correct answer in a row");
                //            tv_counter.setText(Integer.toString(words[randomNum].get_wrong_count()) + " correct answer in a row");
            } else {
                //            tv_counter.setText(Integer.toString(words[randomNum].get_status()) + " correct answers in a row");
                tv_counter.setText(Integer.toString(words_list.get(randomNum).get_status()) + " correct answers in a row");
            }

            //        current_word_id = words[randomNum].get_id();
            current_word_id = words_list.get(randomNum).get_id();
        }
    }

    public void onRadioButtonClicked(View v){}


    @Override
    public void onClick(View v) {

        word current_word = null;
//        for (int i = 0; i<words.length; i++){
        for (int i = 0; i<words_list.size(); i++){
//            if (current_word_id == words[i].get_id()) {
            if (words_list.get(i)!=null){
                if (current_word_id == words_list.get(i).get_id()) {
    //                current_word = words[i];
                    current_word = words_list.get(i);
                    break;
                }
            }
        }

        switch (v.getId()){
            case R.id.btn_hint:

                btn_know.setEnabled(false);
                btn_still_learning.setText("next");
                btn_know.setBackgroundColor(getResources().getColor(R.color.know_btn_inactive));
                String answer_str;
                int length;

                if(tv_question.getText().toString().equals(current_word.get_de())){
                    answer_str = current_word.get_ru();
                    length = answer_str.length();
                    }
                else {
                    answer_str = current_word.get_de();
                    length = answer_str.length();
                }

                String tv_answer_contents = tv_answer.getText().toString();
                if (tv_answer_contents.length()<length) {
                    String firstLetters = Character.toString(answer_str.charAt(tv_answer_contents.length()));
                    tv_answer.setText(tv_answer_contents.concat(firstLetters));
                }
                break;

            case R.id.btn_still_learning:
                current_word.status_to_zero();
                current_word.increment_wrong_count();
                update_word(current_word.get_id(), current_word.get_timing(), current_word.get_status(),
                            current_word.get_right_count(), current_word.get_wrong_count());
                btn_know.setEnabled(true);
                btn_still_learning.setText("still learning");
                btn_know.setBackgroundColor(getResources().getColor(R.color.know_btn_active));

                if(tv_question.getText().toString().equals(current_word.get_de())){
                    tv_answer.setText(current_word.get_ru());
                }
                else {
                    tv_answer.setText(current_word.get_de());
                }
                get_question_mode();
                btn_know.setEnabled(false);         //make button inactive to prevent excessive clicking

                timer = new Timer();
                mTimerTask = new MyTimerTask();
                timer.schedule(mTimerTask, 1000);

                break;

            case R.id.btn_know:
                current_word.increment_status();
                current_word.increment_right_count();

                current_word.set_timing();
                update_word(current_word.get_id(), current_word.get_timing(), current_word.get_status(),
                            current_word.get_right_count(), current_word.get_wrong_count());

                if (current_word.get_status()>=5){
                    int ind = current_word.get_array_ind();
//                    words[current_word.get_array_ind()] = get_word(1);
                    word transit_word = get_word(1);
                    words_list.set(current_word.get_array_ind(), transit_word);
//                    words[current_word.get_array_ind()].set_array_ind(ind);
                    if (transit_word != null) {
//                        word the = words_list.get(current_word.get_array_ind());
                        words_list.get(current_word.get_array_ind()).set_array_ind(ind);
                    }
                }

                if(tv_question.getText().toString().equals(current_word.get_de())){
                    tv_answer.setText(current_word.get_ru());
                }
                else {
                    tv_answer.setText(current_word.get_de());
                }
                get_question_mode();
                btn_know.setEnabled(false);         //make button inactive to prevent excessive clicking

                timer = new Timer();
                mTimerTask = new MyTimerTask();
                timer.schedule(mTimerTask, 10);
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {

        word current_word = null;
        for (int i = 0; i < words.length; i++) {
            if (current_word_id == words[i].get_id()) {
                current_word = words[i];
                break;
            }
        }
            btn_know.setEnabled(false);
            btn_still_learning.setText("next");
            btn_know.setBackgroundColor(getResources().getColor(R.color.know_btn_inactive));

            if (tv_question.getText().toString().equals(current_word.get_de())) {
                tv_answer.setText(current_word.get_ru());

            } else {
                tv_answer.setText(current_word.get_de());

            }
        return true;
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            int i=0;
            h.sendEmptyMessage(i);

        }
    }
}