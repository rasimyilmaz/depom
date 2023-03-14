package com.rasimyilmaz.depom;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//example.androidhive

public class Action_ListActivity extends ListActivity
{
    private ProgressDialog pDialog;
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> productsList;
    // url to get all products list
    private static String url_get_actions = "http://88.247.205.145:18600/get_actions.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ACTIONS = "actions";
    private static final String TAG_GFiyat = "GFiyat";
    private static final String TAG_GMiktar = "GMiktar";
    private static final String TAG_GSatici = "GSatici";
    private static final String TAG_GTarih = "GTarih";
    private String Barcode="";
    private String GKod="";
    private String Gisim="";
    private String GKonum="";
    // products JSONArray
    JSONArray products = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_list);
        productsList = new ArrayList<HashMap<String, String>>();
        Bundle bundle = getIntent().getExtras();
        TextView textView_Gisim=(TextView)findViewById(R.id.textView_Gisim);
        GKod=bundle.getString("GKod");
        Log.d("GKod",GKod);
        Gisim=bundle.getString("Gisim");
        GKonum=bundle.getString("GKonum");
        textView_Gisim.setText(Gisim);
        new LoadAllProducts().execute();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
    class LoadAllProducts extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Action_ListActivity.this);
            pDialog.setMessage("İşleminiz yapılıyor...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //String Keyword=Keyword1.getText().toString();
            params.add(new BasicNameValuePair("Kod", GKod));
            params.add(new BasicNameValuePair(GKonum, "1"));
            Log.d(GKod,GKonum);
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_get_actions, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
                productsList.clear();
                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_ACTIONS);

                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String GFiyat = c.getString(TAG_GFiyat);
                        String GMiktar = c.getString(TAG_GMiktar);
                        String GSatici = c.getString(TAG_GSatici).substring(0,20);
                        String GTarih = c.getString(TAG_GTarih);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
                        // adding each child node to HashMap key => value
                        map.put(TAG_GFiyat, GFiyat);
                        map.put(TAG_GMiktar, GMiktar);
                        map.put(TAG_GSatici, GSatici);
                        map.put(TAG_GTarih, GTarih);
                        // adding HashList to ArrayList
                        productsList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
             runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            Action_ListActivity.this, productsList,
                            R.layout.action_list_item, new String[]{TAG_GFiyat,
                            TAG_GMiktar, TAG_GSatici, TAG_GTarih},
                            new int[]{R.id.textView_GFiyat, R.id.textView_GMiktar, R.id.textView_GSatici, R.id.textView_GTarih});
                    setListAdapter(adapter);
                }
            });

        }

    }
}