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
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    RadioGroup radioGroup;
    RadioButton radioButton, adminRadioButton, userRadioButton;

    private TextView signInText;
    private EditText usernameEv, emailEv, passwordEv;
    private TextInputLayout userNameValTv, emailValTv, passwordValTv, radioValTv;
    Button signUp;

    int radioId, roleId;
    String role, timestamp;

    //db helper
    private DbHelper dbHelper;
    //input validator
    private InputValidation inputValidation;
    //user model
    private UserModel userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        initViews();
        initObject();
        initListeners();


    }

    private void initViews(){
        radioGroup = findViewById(R.id.userCheckBox);
        signInText = findViewById(R.id.signIn);
        signUp = findViewById(R.id.registerButton);
        usernameEv = findViewById(R.id.username);
        emailEv = findViewById(R.id.email);
        passwordEv = findViewById(R.id.password);
        userNameValTv =findViewById(R.id.userNameVal);
        emailValTv =findViewById(R.id.EmailNameVal);
        passwordValTv =findViewById(R.id.passwordVal);
        radioValTv =findViewById(R.id.RadioVal);
        adminRadioButton = (RadioButton) findViewById(R.id.adminRadioButton);
        userRadioButton = (RadioButton) findViewById(R.id.userRadioButton);

    }

    private void initListeners(){
        signUp.setOnClickListener(this);
        signInText.setOnClickListener(this);
    }

    private void initObject(){
        dbHelper = new DbHelper(this);
        inputValidation = new InputValidation(this);
        userModel = new UserModel();
    }

    public void checkButton(View v){
        radioId = radioGroup.getCheckedRadioButtonId();
        radioButton =findViewById(radioId);
        //Toast.makeText(this, "Selected radio button id is: " + radioId + " text is " + radioButton.getText(), Toast.LENGTH_SHORT).show();
        if(adminRadioButton.isChecked()){
            roleId = 1;
            role = "Admin";
            //Toast.makeText(this, "Selected radio button id is: " + roleId + " text is " + adminRadioButton.getText() + "role is " + role, Toast.LENGTH_SHORT).show();
        }
        else if(userRadioButton.isChecked()){
            roleId = 9;
            role = "User";
            //Toast.makeText(this, "Selected radio button id is: " + roleId + " text is " + adminRadioButton.getText() + "role is " + role, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerButton:
                registerUser();
                break;
            case R.id.signIn:
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
                break;
        }
    }

    private void registerUser(){
        if(!inputValidation.isInputEditTextFilled(usernameEv, userNameValTv, "Enter username")){
            return;
        }
        if(!inputValidation.isInputEditTextEmail(emailEv, emailValTv, "Invalid Email")){
            return;
        }
        if(!inputValidation.isInputEditTextMatches(passwordEv, passwordValTv, "Password length to short")){
            return;
        }
        if(!inputValidation.isInputRadioButtonSelected(userRadioButton, adminRadioButton, radioValTv, "please select user type")){
            return;
        }
        timestamp = ""+System.currentTimeMillis();
        if(!dbHelper.checkUser(emailEv.getText().toString().trim())){
            userModel.setUserName(usernameEv.getText().toString().trim());
            userModel.setEmail(emailEv.getText().toString().trim());
            userModel.setPassword(passwordEv.getText().toString().trim());
            userModel.setRoleId(roleId);
            userModel.setRole(role.trim());
            userModel.setAddedTimeStamp(timestamp);

            dbHelper.insertUser(userModel);
            Toast.makeText(this, "New" + role + "Registered", Toast.LENGTH_SHORT).show();
            emptyInputEditText();

        }
        else{
            Toast.makeText(this, "Registration failed! User already exists", Toast.LENGTH_LONG).show();
        }
    }

    private void emptyInputEditText(){
        usernameEv.setText(null);
        emailEv.setText(null);
        passwordEv.setText(null);
    }
}
