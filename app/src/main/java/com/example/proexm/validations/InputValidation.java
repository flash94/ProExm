package com.example.proexm.validations;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

public class InputValidation {
    private Context context;
    public InputValidation(Context context){
        this.context = context;
    }

    public boolean isInputEditTextFilled(EditText editText, TextInputLayout textInputLayout, String message){
        String value = editText.getText().toString().trim();
        if(value.isEmpty()){
            textInputLayout.setError(message);
            hideKeyboardFrom(editText);
            return false;
        }else{
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    public boolean isInputEditTextEmail(EditText editText, TextInputLayout textInputLayout, String message){
        String value = editText.getText().toString().trim();
        if(value.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            textInputLayout.setError(message);
            hideKeyboardFrom(editText);
            return false;
        }else{
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    public boolean isInputEditTextMatches(EditText editText, TextInputLayout textInputLayout, String message){
        String value1 = editText.getText().toString().trim();
        //String value2 = editText2.getText().toString().trim();
        if(value1.length()<5){
            textInputLayout.setError(message);
            hideKeyboardFrom(editText);
            return false;
        }else{
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    public boolean isInputRadioButtonSelected(RadioButton radioButton, RadioButton radioButton2, TextInputLayout textInputLayout, String message){
        if(!radioButton.isChecked() && !radioButton2.isChecked()){
            textInputLayout.setError(message);
            //hideKeyboardFrom(editText);
            return false;
        }
        //String value1 = editText.getText().toString().trim();
        //String value2 = editText2.getText().toString().trim();
        else{
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }

    private void hideKeyboardFrom(View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


}
