package com.example.proexm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proexm.database.DbHelper;
import com.example.proexm.models.UserModel;
import com.example.proexm.validations.InputValidation;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    RadioGroup radioGroup;
    RadioButton radioButton, adminRadioButton, userRadioButton;
    int radioId;
    private String role;

    private TextView signUpText;
    private EditText usernameEv, passwordEv;
    int roleId;
    Button login;

    //db helper
    private DbHelper dbHelper;
    //input validator
    private InputValidation inputValidation;
    //user model
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        initViews();
        initObject();
        initListeners();
    }

    private void initViews() {
        radioGroup = findViewById(R.id.userCheckBox);
        signUpText = findViewById(R.id.signUp);
        login = findViewById(R.id.loginButton);
        usernameEv = findViewById(R.id.username);
        passwordEv = findViewById(R.id.password);
        adminRadioButton = (RadioButton) findViewById(R.id.adminRadioButton);
        userRadioButton = (RadioButton) findViewById(R.id.userRadioButton);
    }

    private void initObject() {
        dbHelper = new DbHelper(this);
        inputValidation = new InputValidation(this);
        userModel = new UserModel();
    }

    private void initListeners(){
        login.setOnClickListener(this);
        signUpText.setOnClickListener(this);
    }

    public void checkButton(View v){
        radioId = radioGroup.getCheckedRadioButtonId();
        radioButton =findViewById(radioId);
        //Toast.makeText(this, "Selected radio button id is: " + radioId + " text is " + radioButton.getText(), Toast.LENGTH_SHORT).show();
        if(adminRadioButton.isChecked()){
            roleId = 1;
            role = "Admin";
        }
        else if(userRadioButton.isChecked()){
            roleId = 9;
            role = "User";
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginButton:
                loginUser();
                break;
            case R.id.signUp:
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                finish();
                break;
        }
    }

    private void loginUser(){
        if(dbHelper.checkUser(usernameEv.getText().toString(),passwordEv.getText().toString(),roleId)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("RoleID", roleId);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Login Failed! Invalid Login credentials", Toast.LENGTH_LONG).show();
        }
    }

    private void emptyInputFields(){
        usernameEv.setText(null);
        passwordEv.setText(null);
    }
}
