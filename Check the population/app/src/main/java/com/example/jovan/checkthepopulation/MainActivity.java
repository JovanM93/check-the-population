package com.example.jovan.checkthepopulation;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText countryEditText;
    EditText ageEditText;
    EditText yearEditText;

    TextView textView;

    public void checkPopulation(View view) {


        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(countryEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(ageEditText.getWindowToken(), 0);
        mgr.hideSoftInputFromWindow(yearEditText.getWindowToken(), 0);

        String encodedCountryName = null;
        try {
            encodedCountryName = URLEncoder.encode(countryEditText.getText().toString(), "UTF-8");
            StringBuilder stringBuilder = new StringBuilder(encodedCountryName);
            boolean isLowerCase = Character.isLowerCase(countryEditText.getText().toString().charAt(0));
            if(isLowerCase){
                encodedCountryName = encodedCountryName.substring(0,1).toUpperCase() + encodedCountryName.substring(1);
                Log.d("CountryName",encodedCountryName);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedYear = null;
        try {
            encodedYear = URLEncoder.encode(yearEditText.getText().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedAge = null;
        try {
            encodedAge = URLEncoder.encode(ageEditText.getText().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //final String encodedCountryName = countryEditText.getText().toString();
        // final String encodedYear = yearEditText.getText().toString();
        //final String encodedAge = ageEditText.getText().toString();

        int year = Calendar.getInstance().get(Calendar.YEAR);

        if (TextUtils.isEmpty(encodedCountryName)) {
            countryEditText.setError("Please enter a valid country name, or pick it from the list");
        } else if (TextUtils.isEmpty(encodedYear) ||  1950 > Integer.parseInt(encodedYear) ||  Integer.parseInt(encodedYear)> year) {
            yearEditText.setError("Please enter a valid year");
        } else if (TextUtils.isEmpty(encodedAge) || 0 > Integer.parseInt(encodedAge) || Integer.parseInt(encodedAge)> 100) {
            ageEditText.setError("Please enter a valid age");
        } else {


            String urlStr = "http://api.population.io:80/1.0/population/" + encodedYear + "/" + encodedCountryName + "/" + encodedAge + "/?format=json";
            String urlS = urlStr.replace("+", "%20");

            final RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest postRequest = new StringRequest(Request.Method.GET, urlS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);


                                Log.d("JsonArrayyy", jsonArray.toString());
                                Log.d("JsonObjecttt", jsonObject.toString());


                                int females = jsonObject.getInt("females");
                                String country = jsonObject.getString("country");
                                int age = jsonObject.getInt("age");
                                int males = jsonObject.getInt("males");
                                int year = jsonObject.getInt("year");
                                int total = jsonObject.getInt("total");

                                String populationDetail = country + " had " + females + " females, " + males + " males" + " at the age of " + age + " in the year of " + year + ", which is total " + total + " people with that age.";
                                textView.setText(populationDetail);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),"Wrong country name, try to find it from the list.",Toast.LENGTH_LONG).show();
                            Log.d("ERROR", "error => " + error.toString());
                        }
                    }

            ) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();


                    return params;
                }
            };
            queue.add(postRequest);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        countryEditText = (EditText)findViewById(R.id.editText);
        ageEditText = (EditText)findViewById(R.id.editText3);
        yearEditText = (EditText)findViewById(R.id.editText2);

        textView = (TextView)findViewById(R.id.resultTextView);

    }

    public void countriesList(View view){
        String urlStr = "http://api.population.io/1.0/countries/?format=json";
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.GET, urlStr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            Log.d("JsonObjecttt", jsonObject.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("countries");
                            final String[] countriesList = new String[jsonArray.length()];

                            for(int i=0;i<jsonArray.length();i++){
                                countriesList[i] = jsonArray.getString(i);
                            }

                            builder.setTitle("Pick a country");
                            builder.setItems(countriesList, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int item) {

                                    countryEditText.setText(countriesList[item]);

                                }

                            });

                            AlertDialog alert = builder.create();

                            alert.show();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }

        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();


                return params;
            }
        };
        queue.add(postRequest);

    }


}

