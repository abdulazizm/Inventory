package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.out;


@RequiresApi(api = Build.VERSION_CODES.M)
public class LoginActivity extends AppCompatActivity {

    EditText mTextPassword,mTextName;
    ImageView mImageViewLogin;
    TextView mTextViewRegister, mTextviewForgotPass;
    //Database db;
    LocalDb db;

    // Declare a string variable for the key we’re going to use in our fingerprint authentication
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    String usr,pwd,mail_id;
    ProgressDialog progressDialog;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //db = new Database(this);
        db = new LocalDb(this);
        if(!(db.alreadyRegistered()&&db.checkUserLoggedin())) {
            mTextPassword = findViewById(R.id.editText_password);
            mTextName = findViewById(R.id.editText_name);

            mImageViewLogin = findViewById(R.id.login);
            mTextViewRegister = findViewById(R.id.textview_register);
            mTextviewForgotPass = findViewById(R.id.textview_forgot_pass);
            mTextViewRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!db.alreadyRegistered()) {
                        //Toast.makeText(LoginActivity.this, "You have registered", Toast.LENGTH_SHORT).show();
                        Intent registerintent = new Intent(LoginActivity.this, RegisterActivity.class);
                        startActivity(registerintent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "You have already registered", Toast.LENGTH_SHORT).show();
                    }
                }


            });

            mTextviewForgotPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup("Enter Registered Email ID");
                }
            });

            mImageViewLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwd = mTextPassword.getText().toString().trim();
                    usr = mTextName.getText().toString().trim();
                    if (pwd.length() >= 7 &&pwd.length() <= 14 && usr.length() >= 4) {
                        if (db.alreadyRegistered()) {

                            if (db.checkUser(usr, pwd)) {
                                Toast.makeText(LoginActivity.this, "Login Successful! Welcome Back!", Toast.LENGTH_SHORT).show();
                                Intent Homepage = new Intent(LoginActivity.this, BottomNav.class);
                                //db.addUser(usr, pwd,null,null,null);
                                db.login();
                                startActivity(Homepage);
                                finish();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setCancelable(false);
                                builder.setTitle("Login Failed");
                                builder.setMessage("Unauthorized Login");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.setIcon(getResources().getDrawable(R.drawable.ic_add));
                                builder.show();
                            }
                        } else {
                            progressDialog = ProgressDialog.show(LoginActivity.this, "Checking Credentials", null, false, true);
                            new LoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("Login Failed");
                        builder.setMessage("Minimum requirement doesn't met");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setIcon(getResources().getDrawable(R.drawable.ic_add));
                        builder.show();
                    }

                }
            });


            // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
            // or higher before executing any fingerprint-related code
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Get an instance of KeyguardManager and FingerprintManager//
                keyguardManager =
                        (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                fingerprintManager =
                        (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

                textView = (TextView) findViewById(R.id.finger_textView);

                if (!db.alreadyRegistered()){
                    textView.setText("Welcome to OpenSourceInventory! Please Login/Register");
                } else if (!fingerprintManager.isHardwareDetected()) {
                    // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                    textView.setText("Your device doesn't support fingerprint authentication");
                }
                //Check whether the user has granted your app the USE_FINGERPRINT permission//
                else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    // If your app doesn't have this permission, then display the following text//
                    textView.setText("Please enable the fingerprint permission");
                }

                //Check that the user has registered at least one fingerprint//
                else if (!fingerprintManager.hasEnrolledFingerprints()) {
                    // If the user hasn’t configured any fingerprints, then display the following message//
                    textView.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
                }

                //Check that the lockscreen is secured//
                else if (!keyguardManager.isKeyguardSecure()) {
                    // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                    textView.setText("Please enable lockscreen security in your device's Settings");
                } else
                {
                    try {
                        generateKey();
                    } catch (FingerprintException e) {
                        e.printStackTrace();
                    }

                    if (initCipher()) {
                        //If the cipher is initialized successfully, then create a CryptoObject instance//
                        cryptoObject = new FingerprintManager.CryptoObject(cipher);

                        textView.setText("One-Touch Login Access");
                        // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                        // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                        FingerprintHandler helper = new FingerprintHandler(this);
                        helper.startAuth(fingerprintManager, cryptoObject);
                    } else {
                        textView.setText("Failed to Configure One-Touch Login");
                    }
                }
            }
        } else {
            Intent Homepage = new Intent(LoginActivity.this, BottomNav.class);
            startActivity(Homepage);
            finish();
        }
    }

    void showPopup(String title) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(title);

        final EditText input = new EditText(LoginActivity.this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mail_id = (input.getText().toString().trim());
                loadData();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    void loadData(){
        progressDialog = ProgressDialog.show(LoginActivity.this, "Sending mail", null, false, true);
        new loadURLTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
//Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);

//        System.exit(0);
    }

    class LoginTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);

            //Toast.makeText(getApplicationContext(),""+jsonRead,Toast.LENGTH_SHORT).show();
            String status="",user="",company_name="";
            int cid =0;
            try {
                JSONObject json = new JSONObject(jsonRead);
                status = json.getString("status");
                user = json.getString("user");
                company_name = json.getString("company_name");
                cid = json.getInt("cid");
                //Toast.makeText(getApplicationContext(), status+user+jsonRead, Toast.LENGTH_SHORT).show();
            } catch (JSONException e){
                e.printStackTrace();
            }

            progressDialog.dismiss();
            if(status.equals("success")&&user.equals("exists")){
                    Toast.makeText(LoginActivity.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
                    Intent Homepage = new Intent(LoginActivity.this, BottomNav.class);
                    db.addUser(usr,pwd,company_name, cid,null,null,null);
                    startActivity(Homepage);
                    finish();
            }
            else if(status.equals("success")&&user.equals("not exist")){

                AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Login failed");
                builder.setMessage("No User Exists. Wrong Credentials Provided");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setIcon(getResources().getDrawable(R.drawable.ic_lightbulb_on ));
                builder.show();

            }
            else{
                // Toast.makeText(RegisterActivity.this,"try again later", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Login failed");
                builder.setMessage("Network Connectivity Error. Please try again later");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setIcon(getResources().getDrawable(R.drawable.ic_lightbulb_off ));
                builder.show();
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                //return "{\"status\":\"success\",\"user\":\"exists\",\"cid\":\"1,\"company_name\":\"Test1\"}";
//https://www.fingerpointengg.com/Android_App/InventoryManagement/onLogin.php?p_user=aziz&p_pass=1234
                String data = URLEncoder.encode("p_user", "UTF-8")
                        + "=" + URLEncoder.encode(usr, "UTF-8");

                data += "&" + URLEncoder.encode("p_pass", "UTF-8")
                        + "=" + URLEncoder.encode(pwd, "UTF-8");

                URL url = new URL("https://www.fingerpointengg.com/Android_App/InventoryManagement/onLogin.php");
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
            return "{\"status\":\"failed\"}";
        }
        }

    class loadURLTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);
            //Toast.makeText(getApplicationContext(), jsonRead, Toast.LENGTH_SHORT).show();
            String status = "";
            int total=0;
            try {
                JSONObject json = new JSONObject(jsonRead);
                status = json.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
            if (status.equals("success")) {

                android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Search failed");
                builder.setMessage("Reset email has sent to your mail id");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setIcon(getResources().getDrawable(R.drawable.ic_add ));
                builder.show();

            } else {

                android.support.v7.app.AlertDialog.Builder builder=new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Invalid mail id");
                builder.setMessage("May be Incorrect Mail ID/Network Connectivity Error. Please try again later");
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
        protected String doInBackground(String... strings) {

            try {

                String data = URLEncoder.encode("mail_id", "UTF-8")
                        + "=" + URLEncoder.encode(mail_id, "UTF-8");

//                data += "&" + URLEncoder.encode("product_category", "UTF-8")
//                        + "=" + URLEncoder.encode(String.valueOf(product_category), "UTF-8");
//
//                data += "&" + URLEncoder.encode("name", "UTF-8")
//                        + "=" + URLEncoder.encode(u_input, "UTF-8");

                URL url = new URL("https://www.fingerpointengg.com/Android_App/InventoryManagement/forgot_password.php");
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
            return "{\"status\":\"failed\"}";
        }

    }
    }
