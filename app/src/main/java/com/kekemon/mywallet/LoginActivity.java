package com.kekemon.mywallet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginActivity extends AppCompatActivity {

    private EditText eMobile, ePassword;
    private Button loginBtn;
    private String mobile;
    private String password;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eMobile = (EditText)findViewById(R.id.mobileField);
        ePassword = (EditText)findViewById(R.id.passwordField);
        loginBtn = (Button)findViewById(R.id.button);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
    }

    private void checkLogin() {

        if (!validateLoginData()) return;

        mobile = eMobile.getText().toString();
        password = ePassword.getText().toString();

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Starting...");
        progressDialog.show();

        eMobile.getText().clear();
        ePassword.getText().clear();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        JSONObject jsonParam = new JSONObject();
                        try {
                            jsonParam.put("mobile", mobile);
                            jsonParam.put("password", password);
                            sendPost("http://192.168.1.125:8080/WalletWebService/Login",jsonParam);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, 3000);

    }

    private boolean validateLoginData() {
        boolean valid = true;

        String email = eMobile.getText().toString();
        String password = ePassword.getText().toString();

        if (email.isEmpty() || !Patterns.PHONE.matcher(email).matches()) {
            eMobile.setError("Enter a valid Phone number");
            valid = false;
        } else {
            eMobile.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            ePassword.setError("Between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            ePassword.setError(null);
        }

        return valid;
    }

    public void sendPost(final String urlAddress,final JSONObject jsonParam) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAddress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());
                    os.flush();
                    os.close();

                    if (conn.getResponseCode() == 200){
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        String jsonMessage = "";
                        while ((inputLine = in.readLine()) != null) {
                            jsonMessage += inputLine;
                        }
                        Log.i("EMON", jsonMessage);
                        Intent intent =new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("IDENT","");
                        if (progressDialog != null) progressDialog.dismiss();
                        startActivity(intent);
                        in.close();
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
