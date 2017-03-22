package com.innovativeincarnates.yash.customer;

/**
 * Created by yash on 22/3/17.
 */
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class Notification_wifi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);
        String s= getIntent().getStringExtra("Address");
        String[] info = s.split("\\$");
        TextView shopInfo = (TextView) findViewById(R.id.textView);
        String display = info[0] + " available at " + info[1] + "\n Address: " + info[2];
        shopInfo.setText(display);
    }

}
