package com.rasimyilmaz.depom;
//example.androidhive

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.manateeworks.cameraDemo.ActivityCapture;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class AllProductsActivity extends ListActivity
{
    static final int SCAN_BARCODE_REQUEST = 320;
	// Progress Dialog
	private ProgressDialog pDialog;
	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();
	ArrayList<HashMap<String, String>> productsList;
	// url to get all products list
	private static String url_all_products = "http://88.247.205.145:18600/get_all_products.php";
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCTS = "products";
    private static final String TAG_Kod = "Kod";
    private static final String TAG_isim = "isim";
    private static final String TAG_Barkod = "Barkod";
    private static final String TAG_Fiyat = "Fiyat";
    private static final String TAG_Miktar = "Miktar";
    private static final String TAG_Doviz = "Doviz";
    private static final String TAG_Birim = "Birim";
    private static final String TAG_Konum="Konum";
    private String Barcode="";
    // products JSONArray
	JSONArray products = null;
    public void Ara(View v){
        new LoadAllProducts().execute();
        EditText myEditText = (EditText) findViewById(R.id.editText);
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }
    public void inputBarcode(View v){
        Barcode="";
        Intent intent=new Intent(this, ActivityCapture.class);
        startActivityForResult(intent,SCAN_BARCODE_REQUEST);
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_products);
		// Hashmap for ListView
		productsList = new ArrayList<HashMap<String, String>>();
		// Loading products in Background Thread
		// Get listview
		ListView lv = getListView();

		// on seleting single product
		// launching Edit Product Screen
        final EditText SearcheditText = (EditText) findViewById(R.id.editText);
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                SearcheditText.setText("");
                return true;
            }
        });
        SearcheditText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // getting values from selected ListItem
                String GKod = ((TextView) view.findViewById(R.id.textView_Kod)).getText().toString();
                String Gisim = ((TextView) view.findViewById(R.id.textView_isim)).getText().toString();
                String GKonum = ((TextView) view.findViewById(R.id.textView_Konum)).getText().toString();
                Intent in = new Intent(getApplicationContext(),
                        Action_ListActivity.class);
                // sending pid to next activity
                in.putExtra("GKod", GKod);
                in.putExtra("Gisim", Gisim);
                in.putExtra("GKonum", GKonum);
                startActivity(in);
                return true;
            }
        });
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(TAG_Barkod,((TextView) view.findViewById(R.id.textView_Barkod)).getText().toString());
                    clipboard.setPrimaryClip(clip);
                } else {
                    final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setText(((TextView) view.findViewById(R.id.textView_Barkod)).getText().toString());
                }
            }
        });
	}

	// Response from Edit Product Activity
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
        if (requestCode == SCAN_BARCODE_REQUEST) {
            if (resultCode== Activity.RESULT_OK){
                EditText Keyword1 = (EditText) findViewById(R.id.editText);
                Barcode = data.getData().toString();
                Keyword1.setText(Barcode);
                Ara(Keyword1);
            }
        }
    }
        class LoadAllProducts extends AsyncTask<String, String, String> {

            /**
             * Before starting background thread Show Progress Dialog
             */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(AllProductsActivity.this);
                pDialog.setMessage("Ürünler yükleniyor. Lütfen Bekleyeniz...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            /**
             * getting All products from url
             */
            protected String doInBackground(String... args) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                //String Keyword=Keyword1.getText().toString();

                EditText Keyword1 = (EditText) findViewById(R.id.editText);
                String Keyword = Keyword1.getText().toString();
                CheckBox checkboxAyd=(CheckBox)findViewById(R.id.checkBoxAyd);
                CheckBox checkboxIzm=(CheckBox)findViewById(R.id.checkBoxIzm);
                params.add(new BasicNameValuePair("ara", Keyword));
                if (checkboxAyd.isChecked()){
                    params.add(new BasicNameValuePair("Ayd","1"));}
                if (checkboxIzm.isChecked()){
                    params.add(new BasicNameValuePair("Izm","1"));}

                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

                // Check your log cat for JSON reponse
                Log.d("All Products: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);
                    productsList.clear();
                    if (success == 1) {
                        // products found
                        // Getting Array of Products
                        products = json.getJSONArray(TAG_PRODUCTS);

                        // looping through All Products
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            String Kod = c.getString(TAG_Kod);
                            String isim = c.getString(TAG_isim);
                            String Barkod = c.getString(TAG_Barkod);
                            String Fiyat = c.getString(TAG_Fiyat);
                            String Doviz = c.getString(TAG_Doviz);
                            String Miktar = c.getString(TAG_Miktar);
                            String Birim = c.getString(TAG_Birim);
                            String Konum = c.getString(TAG_Konum);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_Kod,Kod);
                            map.put(TAG_isim, isim);
                            map.put(TAG_Barkod, Barkod);
                            map.put(TAG_Fiyat, Fiyat + Doviz);
                            map.put(TAG_Miktar, Miktar + Birim);
                            map.put(TAG_Konum,Konum);

                            // adding HashList to ArrayList
                            productsList.add(map);
                        }
                    } else {
                        // no products found
                        // Launch Add New product Activity
                    /*(Intent i = new Intent(getApplicationContext(),
							NewProductActivity.class);
					// Closing all previous activities
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);)*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            /**
             * After completing background task Dismiss the progress dialog
             * *
             */
            protected void onPostExecute(String file_url) {
                // dismiss the dialog after getting all products
                pDialog.dismiss();
                // updating UI from Background Thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        ListAdapter adapter = new SimpleAdapter(
                                AllProductsActivity.this, productsList,
                                R.layout.list_item, new String[]{TAG_isim,
                                TAG_Barkod, TAG_Fiyat, TAG_Miktar,TAG_Konum,TAG_Kod },
                                new int[]{R.id.textView_isim, R.id.textView_Barkod, R.id.textView_Fiyat, R.id.textView_Miktar,R.id.textView_Konum,R.id.textView_Kod}){
                        @Override
                            public View getView(int position, View convertView, android.view.ViewGroup parent) {

                                View view = super.getView(position, convertView, parent);
                                TextView text_Konum = (TextView) view.findViewById(R.id.textView_Konum);
                                TextView text_isim = (TextView) view.findViewById(R.id.textView_isim);
                                if(text_Konum.getText().toString().equals("Ayd")){
                                   text_isim.setTextColor(Color.parseColor("#FFFFD43B"));
                                }else if (text_Konum.getText().toString().equals("Izm")){
                                   text_isim.setTextColor(Color.WHITE);
                                }
                                return view;
                            }};

                        // updating listview
                        setListAdapter(adapter);
                    }
                });

            }

        }
}