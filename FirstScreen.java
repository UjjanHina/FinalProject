package com.example.mahnoor.project1;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class FirstScreen extends Activity {
    Button btn1admin,btn2cust,btn3driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        btn1admin=(Button)findViewById(R.id.button4);
        btn2cust=(Button)findViewById(R.id.button5);
        btn3driver=(Button)findViewById(R.id.button3);
        btn1admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(FirstScreen.this,AdminLogInActivity.class);
                startActivity(i);
            }
        });

      btn2cust.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent i=new Intent(FirstScreen.this,CustomerMapsActivity.class);
              startActivity(i);
          }
      });

      btn3driver.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent i=new Intent(FirstScreen.this,DeliveryBoyMapsActivity.class);
              startActivity(i);
          }
      });
    }



}
