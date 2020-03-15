package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.out;

public class Inventory extends AppCompatActivity {

    CardView card_add_product, card_add_category, card_update_category, card_update_product;
    private String u_input = "";
    //int product_category =0,add_new=0;

    ProgressDialog progressDialog;
    Bundle b_product_category;
    LocalDb db;
    //cid=256&add_new=1&product_category=0&name=biscuit&
    //image=%22https://cds.com%22&description=%22chjsj%22&stock=5&
    // price=20&status=ACTIVE&rank=2&category=Snacks&id=1

    String name, image, description, status_s, category_s;
    int cid, stock_i, price_i, rank_i, id, product_category, add_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        db = new LocalDb(getApplicationContext());
        card_add_category = findViewById(R.id.card_add_category);
        card_add_product = findViewById(R.id.card_add_product);
        card_update_category = findViewById(R.id.card_update_category);
        card_update_product = findViewById(R.id.card_update_product);

        b_product_category = new Bundle();

        update_data();

        card_add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_category = 1;
                add_new = 1;
                callUpdateInventory();
                //showPopup("Enter new category name");
            }
        });

        card_add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_category = 0;
                add_new = 1;
                callUpdateInventory();
                //showPopup("Enter new product name");
            }
        });

        card_update_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_category = 1;
                add_new = 0;
                showPopup("Enter name of category to edit");
            }
        });

        card_update_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product_category = 0;
                add_new = 0;
                showPopup("Enter name of product to edit");
            }
        });

    }

    void callUpdateInventory(){
        Intent inv_mgmt = new Intent(getApplicationContext(), UpdateInventory.class);
        b_product_category.putInt("add_new", add_new);
        b_product_category.putInt("product_category", product_category);
        inv_mgmt.putExtras(b_product_category);
        startActivity(inv_mgmt);
    }

    void loadData(){
        progressDialog = ProgressDialog.show(Inventory.this, "Searching", null, false, true);
        new Inventory.loadURLTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    void showPopup(String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Inventory.this);
        builder.setTitle(title);

        final EditText input = new EditText(Inventory.this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                u_input = (input.getText().toString().trim());
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

    void update_data() {

        b_product_category.putInt("cid", cid);
        b_product_category.putString("name", name);
        b_product_category.putString("image", image);
        b_product_category.putString("description", description);
        b_product_category.putString("category", category_s);
        b_product_category.putString("status", status_s);
        b_product_category.putInt("stock", stock_i);
        b_product_category.putInt("price", price_i);
        b_product_category.putInt("rank", rank_i);
        b_product_category.putInt("id", id);

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
                total = json.getInt("Total");
                //{"status":"success","prod_id0":"1","prod_price0":"20","prod_status0":"ACTIVE","prod_stock0":"5",
                //"prod_name0":"bisscuit","prod_description0":"chjsj","prod_image0":"https://cds.com","prod_category0":"Snacks","Total":"1"}
                if(product_category==1) {
                    id = json.getInt("category_id0");
                    name = json.getString("category_name0");
                    category_s = json.getString("status");
                    description = json.getString("category_description0");
                    rank_i = json.getInt("category_rank0");
                    image = json.getString("category_image0");

                } else if(product_category==0){

                    id = json.getInt("prod_id0");
                    name = json.getString("prod_name0");
                    category_s = json.getString("prod_category0");
                    description = json.getString("prod_description0");
                    image = json.getString("prod_image0");
                    price_i = json.getInt("prod_price0");
                    status_s = json.getString("prod_status0");
                    stock_i = json.getInt("prod_stock0");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
            if (status.equals("success") && total>0) {

                Intent inv_mgmt = new Intent(getApplicationContext(), UpdateInventory.class);
                update_data();
                b_product_category.putString("u_input", u_input);
                b_product_category.putInt("add_new", add_new);
                b_product_category.putInt("product_category", product_category);
                inv_mgmt.putExtras(b_product_category);
                startActivity(inv_mgmt);

            } else if (total==0) {
                AlertDialog.Builder builder=new AlertDialog.Builder(Inventory.this);
                builder.setCancelable(false);
                builder.setTitle("Search failed");
                builder.setMessage("Invalid Search Keyword. Please try a different Keyword");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setIcon(getResources().getDrawable(R.drawable.ic_add ));
                builder.show();

            } else {

                AlertDialog.Builder builder=new AlertDialog.Builder(Inventory.this);
                builder.setTitle("Search failed");
                builder.setMessage("May be due Network Connectivity Error. Please try again later");
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

                //return  "{\"status\":\"success\",\"prod_id0\":\"1\",\"prod_price0\":\"20\",\"prod_status0\":\"ACTIVE\",\"prod_stock0\":\"5\",\"prod_name0\":\"bisscuit\",\"prod_description0\":\"chjsj\",\"prod_image0\":\"https://cds.com\",\"prod_category0\":\"Snacks\",\"Total\":\"1\"}";

                String data = URLEncoder.encode("cid", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(db.getCID()), "UTF-8");

                data += "&" + URLEncoder.encode("product_category", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(product_category), "UTF-8");

                data += "&" + URLEncoder.encode("name", "UTF-8")
                        + "=" + URLEncoder.encode(u_input, "UTF-8");

                URL url = new URL("https://<your-domain-name>/Android_App/InventoryManagement/get_info.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                urlConnection.setDoOutput(true);

                //return "{\"status\":\"success\",\"category_id0\":\"1\",\"company_name0\":\"Fab\",\"category_name0\":\"Snacks\",\"category_image0\":\"https://cdn2.lamag.com/wp-content/uploads/sites/6/2016/05/Snax1.jpg\",\"category_description0\":\"chjsj\",\"category_rank0\":\"1\",\"Total\":\"1\"}";
                //return "{\"status\":\"success\",\"category_id0\":\"1\",\"company_name0\":\"Test_1\",\"category_name0\":\"Snacks\",\"category_image0\":\"https://image.shutterstock.com/image-vector/sandwich-ingredients-3d-isometric-style-260nw-411111961.jpg\",\"category_description0\":\"scasdcfdvsfesdwec\",\"category_rank0\":\"1\",\"Total\":\"1\"}";
                //return  "{\"status\":\"success\",\"prod_id0\":\"1\",\"prod_price0\":\"20\",\"prod_status0\":\"ACTIVE\",\"prod_stock0\":\"5\",\"prod_name0\":\"bisscuit\",\"prod_description0\":\"chjsj\",\"prod_image0\":\"https://cds.com\",\"prod_category0\":\"Snacks\",\"Total\":\"1\"}";
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