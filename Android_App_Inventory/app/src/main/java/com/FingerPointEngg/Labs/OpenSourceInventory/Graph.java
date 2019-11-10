package com.FingerPointEngg.Labs.OpenSourceInventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static java.lang.System.out;


public class Graph extends Fragment {

    String u_input;
    ProgressDialog progressDialog;
    LocalDb db;
    //{"status":"success","data":"uploaded"}
    List<PointValue> yAxisValues = new ArrayList<>();
    LineChartView lineChartView;
    Axis yAxis = new Axis();
    Axis xAxis = new Axis();
    List<AxisValue> axisValues = new ArrayList<AxisValue>();
    Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));
    List<Line> lines = new ArrayList<>();
    LineChartData data = new LineChartData();

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_graph,container,false);

        db = new LocalDb(getContext());
        lineChartView = rootView.findViewById(R.id.chart);

        data.setAxisYLeft(yAxis);
        data.setAxisXBottom(xAxis);

        yAxis.setTextSize(16);
        yAxis.setTextColor(Color.parseColor("#03A9F4"));
        yAxis.setName("Sales in Rupee");


        xAxis.setTextSize(16);
        xAxis.setTextColor(Color.parseColor("#03A9F4"));
        xAxis.setName("Timeline");

                        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
                viewport.top =110;
                lineChartView.setMaximumViewport(viewport);
                lineChartView.setCurrentViewport(viewport);


        lines.add(line);


        data.setLines(lines);
        showPopup("Enter category name");




        return rootView;
    }

    void showPopup(String title) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(title);

        final EditText input = new EditText(getContext());

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                u_input = (input.getText().toString());
                progressDialog = ProgressDialog.show(getContext(), "Checking data", null, false, true);
                new LoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    class LoginTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPostExecute(String jsonRead) {

            super.onPostExecute(jsonRead);

            //Toast.makeText(getContext(),""+jsonRead,Toast.LENGTH_SHORT).show();
            String status="";
            try {
                JSONObject json = new JSONObject(jsonRead);
                status = json.getString("status");
                for (int i = 1; i <= 10; i++) {

                    if(json.getString("day" + i)!=null)
                    yAxisValues.add(new PointValue(i, Integer.parseInt(json.getString("day" + i))));

                }
                Toast.makeText(getContext(), "Last 10 Day Sales Data", Toast.LENGTH_SHORT).show();
            } catch (JSONException e){
                e.printStackTrace();
            }

            progressDialog.dismiss();
            //Toast.makeText(getContext(), ""+db.getCID(), Toast.LENGTH_SHORT).show();
            if(status.equals("success")){


                String[] axisData = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct"};

                for (int i = 0; i < axisData.length; i++) {
                    axisValues.add(i,new AxisValue(i).setLabel(axisData[i]));
                }

                lineChartView.setLineChartData(data);

            }
            else{
                // Toast.makeText(RegisterActivity.this,"try again later", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle("Chart Fetch failed");
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

                //https://www.fingerpointengg.com/Android_App/InventoryManagement/sales_list.php?category=Snacks&cid=256
                String data = URLEncoder.encode("cid", "UTF-8")
                        + "=" + URLEncoder.encode(String.valueOf(db.getCID()), "UTF-8");

                data += "&" + URLEncoder.encode("category", "UTF-8")
                        + "=" + URLEncoder.encode(u_input, "UTF-8");

                URL url = new URL("https://www.fingerpointengg.com/Android_App/InventoryManagement/sales_list.php");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                urlConnection.setDoOutput(true);

                //return "{\"status\":\"success\",\"day1\":\"0\",\"day2\":\"0\",\"day3\":\"0\",\"day4\":\"0\",\"day5\":\"0\",\"day6\":\"0\",\"day7\":\"0\",\"day8\":\"0\",\"day9\":\"600\",\"day10\":\"4800\",\"products\":\"10\"}";
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
