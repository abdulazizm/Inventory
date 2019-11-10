package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class HomeFragment extends Fragment {

    private GridView gridView;
    static public CategoriesAdapter gridAdapter;
    static ArrayList imageItems;
    //String[] category = new String[]{ "Benchmarks","Toys","Name Embeded","Smart Gadget","Fashion","Arts & Crafts","Tech-Geeks","Prototype Design","Household"};
    //int[] categoryImage = new int[]{R.drawable.benchmarks,R.drawable.toys,R.drawable.nameembedded,R.drawable.gadgets,R.drawable.fashion,R.drawable.arts,R.drawable.geeks,R.drawable.prototype,R.drawable.household};
    ArrayList categories = new ArrayList();
    ArrayList imageurl = new ArrayList();
    ProgressDialog progressDialog;
    LocalDb db;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.fragment_home, null);

        db = new LocalDb(getContext());
        progressDialog = ProgressDialog.show(getContext(), "Listing your shop categories", null, false, true);
        new CategoriesData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        final Vibrator vib= (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        gridView = view.findViewById(R.id.gridView);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(getActivity(),"Home. Category: "+id,Toast.LENGTH_SHORT).show();
                vib.vibrate(50);
                Bundle data = new Bundle();
                data.putString("category",""+categories.get( (int) id));
                Intent category = new Intent(getActivity(), ProductList.class);
                category.putExtras(data);
                startActivity(category);

            }
        });

        gridAdapter = new CategoriesAdapter(getContext(), R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);

        return view;
    }


    /**
     * Prepare some dummy data for gridview
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ArrayList<ProductListItem> getData() {

        imageItems = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {

            imageItems.add(new ProductListItem(imageurl.get(i).toString(), categories.get(i).toString(), i, categories.get(i).toString()));

        }

        return imageItems;
    }


    class CategoriesData extends AsyncTask<String, String, String> {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);
            //Toast.makeText(getContext(),jsonRead, Toast.LENGTH_SHORT).show();
            int l_categories=0;
            String status = null;
            try {
                JSONObject json = new JSONObject(jsonRead);
                if(json.getString("categories")!=null)
                l_categories = Integer.parseInt(json.getString("categories"));
                status = json.getString("status");

                //pid, title, price, description, imageurl
                for (int i = 0; i < l_categories; i++) {
                    categories.add(i, json.getString("category_name" + i));
                    imageurl.add(i, json.getString("category_image" + i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
            if (status != null && status.equals("success")) {

                CategoriesAdapter gridViewAdapter = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    gridViewAdapter = new CategoriesAdapter(getContext(), R.layout.grid_item_layout, getData());
                }
                gridView.setAdapter(gridViewAdapter);

            } else {
                //Toast.makeText(getContext(),"Status:"+status, Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Category Listing failed");
                builder.setMessage("Network Connectivity Error. Please try again later");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setIcon(getResources().getDrawable(R.drawable.ic_lightbulb_off));
                builder.show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                String data = URLEncoder.encode("data", "UTF-8")
                        + "=" + URLEncoder.encode("xHzy5InventorySec", "UTF-8");
                data += "&" + URLEncoder.encode("cid", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(db.getCID()), "UTF-8");

                URL url = new URL("https://www.fingerpointengg.com/Android_App/InventoryManagement/category_data.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                urlConnection.setDoOutput(true);

                return "{\"status\":\"success\",\"category_name0\":\"Snacks\",\"category_image0\":\"https://cds.com\",\"category_name1\":\"biscuit\",\"category_image1\":\"https://cds.com\",\"categories\":\"2\"}";
                //https://www.fingerpointengg.com/Android_App/InventoryManagement/category_data.php?data=xHzy5InventorySec&cid=256
//                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
//                writer.write(data);
//                writer.flush();
//                writer.close();
//
//                // Get the server response
//                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//                StringBuilder sb = new StringBuilder();
//                String line;
//                // Read Server Response
//                while ((line = reader.readLine()) != null) {
//                    // Append server response in string
//                    sb.append(line).append("\n");
//                }
//                reader.close();
//                return sb.toString();

            } catch (Exception e) {
                //e.println(e.getMessage());
            }
            return "{\"status\":\"failed\"}";
        }
    }

    }
