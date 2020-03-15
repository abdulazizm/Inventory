package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.out;

public class RegisterActivity extends AppCompatActivity {
    LocalDb db;
    EditText mTextUsername;
    EditText mTextPassword;
    EditText mTextCnfPassword, mTextCompName;
    ImageView mImageViewregister;
    TextView mTextViewLogin;
    String username, password, cnf_pwd, company;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new LocalDb(this);
        mTextUsername = findViewById(R.id.editText_username);
        mTextCompName = findViewById(R.id.editText_company);
        mTextPassword = findViewById(R.id.editText_password);
        mTextCnfPassword = findViewById(R.id.editText_cnf_password);
        mImageViewregister = findViewById(R.id.register);
        mTextViewLogin = findViewById(R.id.textview_alregister);

        mTextViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(LoginIntent);
                finish();
            }
        });

        mImageViewregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = mTextUsername.getText().toString().trim();
                password = mTextPassword.getText().toString().trim();
                cnf_pwd = mTextCnfPassword.getText().toString().trim();
                company = mTextCompName.getText().toString().trim();

                if (password.equals(cnf_pwd) && password.length() <= 14 && password.length() >= 7 && username.length() >= 4) {
                    progressDialog = ProgressDialog.show(RegisterActivity.this, "Registering", null, false, true);
                    new RegisterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    TextView hint = findViewById(R.id.textview_hint);
                    hint.setText(R.string.Hint);
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class RegisterTask extends AsyncTask<String, String, String> {


        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);
            //Toast.makeText(getApplicationContext(), jsonRead, Toast.LENGTH_SHORT).show();
            String status = "", user = "";
            int cid = 0;
            try {
                JSONObject json = new JSONObject(jsonRead);
                status = json.getString("status");
                user = json.getString("user");
                cid = json.getInt("cid");
            } catch (JSONException e) {
                e.printStackTrace();
            }

                progressDialog.dismiss();
                if (status.equals("success") && user.equals("created")) {
                    db.addUser(username, password, company, cid, null, null, null);
                    Toast.makeText(RegisterActivity.this, "You have registered successfully", Toast.LENGTH_SHORT).show();
                    Intent getIntoApp = new Intent(RegisterActivity.this, BottomNav.class);
                    startActivity(getIntoApp);
                    finish();
                } else if (user.equals("exists")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Registration failed");
                    builder.setMessage("Username you requested has been taken already. Please try a different username");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setIcon(getResources().getDrawable(R.drawable.ic_add));
                    builder.show();

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("Registration failed");
                    builder.setMessage("Network Connectivity Error. Please try again later");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

            @Override
            protected String doInBackground (String...strings){


                try {

                String data = URLEncoder.encode("p_user", "UTF-8")
                        + "=" + URLEncoder.encode(username, "UTF-8");

                data += "&" + URLEncoder.encode("p_pass", "UTF-8")
                        + "=" + URLEncoder.encode(password, "UTF-8");

                data += "&" + URLEncoder.encode("company_name", "UTF-8")
                        + "=" + URLEncoder.encode(company, "UTF-8");

                URL url = new URL("https://<your-domain-name>/Android_App/InventoryManagement/onRegister.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                urlConnection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(data);
                writer.flush();
                writer.close();

                // Get the server response
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();

                } catch (Exception e) {
                    out.println(e.getMessage());
                }
                return "{status:failed}";
            }
        }
    }