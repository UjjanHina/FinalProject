package com.example.mahnoor.project1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLogInActivity extends Activity implements View.OnClickListener{
    EditText emailtxt,passtxt;
    Button btnlogin;
    TextView textviewsignin;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        setContentView(R.layout.activity_second_screen);
        emailtxt=(EditText)findViewById(R.id.editText3);
        passtxt=(EditText)findViewById(R.id.editText4);
        btnlogin=(Button)findViewById(R.id.button);
        textviewsignin=(TextView)findViewById(R.id.textView4);
        textviewsignin.setOnClickListener(this);
        btnlogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view==btnlogin){
            registerUser();
        }

        if(view==textviewsignin){
            Toast.makeText(this, "already registered", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String email=emailtxt.getText().toString().trim();
        String password=passtxt.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Registering User....");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AdminLogInActivity.this,"Registered Successfully",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Intent intent=new Intent(AdminLogInActivity.this,AdminMapsActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(AdminLogInActivity.this,"Not Registered",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }
}
