package com.example.assignment5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Button searchButton;
    EditText inputField;
    TextView nameField;

    TextView numberField;
    TextView weightField;
    TextView heightField;
    TextView baseXPField;
    TextView moveField;
    TextView abilityField;

    ArrayList<String> pokemonList;
    ArrayAdapter<String> adapter;
    ListView listView;


    /**
     * Searches api for provided Pokemon
     */
    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String name = inputField.getText().toString();
            Log.i("apiCall", "made a request for: " + 1);
            queryAPI(name);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize widgets
        AndroidNetworking.initialize(getApplicationContext());
        searchButton = findViewById(R.id.searchButton);
        inputField = findViewById(R.id.inputField);
        nameField = findViewById(R.id.pokemonName);
        numberField = findViewById(R.id.numberInputView);
        weightField = findViewById(R.id.weightInput);
        heightField = findViewById(R.id.heightInput);
        baseXPField = findViewById(R.id.baseXPInput);
        moveField = findViewById(R.id.moveInput);
        abilityField = findViewById(R.id.abilityInput);

        searchButton.setOnClickListener(searchListener);
        listView = findViewById(R.id.listView);

        Log.i("ListView", "Start initialization");
        //initialize ListView and its ArrayAdapter
        pokemonList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pokemonList);
        listView.setAdapter(adapter);
        Log.i("ListView", "Finished initialization");
        //set item click listener for the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get the selected item's value
                String selectedItemValue = (String) parent.getItemAtPosition(position);

                //update the TextView with the selected item's value
                String name = selectedItemValue.substring(0, selectedItemValue.indexOf("-") - 1);
                queryAPI(name);
            }
        });

        Log.i("apiCall", "Finished OnCreate");
    }

    /**
     * Query API for provided string
     * @param value
     */
    public void queryAPI(String value){
        makeRequest(value);
    }

    private void makeRequest(String ticker){
        ANRequest req = AndroidNetworking.get("https://pokeapi.co/api/v2/pokemon/{id}/")
                .addPathParameter("id", ticker)
                .setPriority(Priority.LOW)
                .build();
        req.getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    //name
                    Log.i("apiCall", "Inside onResponse");
                    String name = response.getString("name");
                    Log.i("apiCall", "Setting nameField to: " + name);
                    nameField.setText(name);

                    //number
                    String id = response.getString("id");
                    Log.i("apiCall", "Setting id to: " + id);
                    numberField.setText(id);

                    //image
                    //modify ID to get image url
                    if(id.length() < 3){
                        for(int i = 0; i <= 3 - id.length(); i++){
                            id = "0" + id;
                        }
                    }
                    Log.i("apiCall", "Setting Image to: " + id + ".png");
                    updateWebsite("https://raw.githubusercontent.com/HybridShivam/Pokemon/master/assets/images/" + id + ".png");

                    //weight
                    String weight = response.getString("weight");
                    weightField.setText(weight);

                    //height
                    String height = response.getString("height");
                    heightField.setText(height);

                    //basexp
                    String baseXP = response.getString("base_experience");
                    baseXPField.setText(baseXP);

                    //move
                    Log.i("move", "Try JSON");
                    JSONArray array = response.getJSONArray("moves");
                    Log.i("move", "JSON Array Initialized");
                    JSONObject moveContainer = array.getJSONObject(0);
                    Log.i("move", moveContainer.toString());
                    JSONObject move = moveContainer.getJSONObject("move");
                    Log.i("move", move.toString());
                    String moveName = move.getString("name");
                    moveField.setText(moveName);


                    //ability
                    Log.i("ability", "Try JSON");
                    JSONArray moves = response.getJSONArray("abilities");
                    Log.i("ability", "JSON Array Initialized");
                    JSONObject abilityContainer = moves.getJSONObject(0);
                    Log.i("ability", abilityContainer.toString());
                    JSONObject ability = abilityContainer.getJSONObject("ability");
                    Log.i("ability", ability.toString());
                    String abilityName = ability.getString("name");
                    abilityField.setText(abilityName);

                    //update list view
                    Log.i("ListView", "Tried to add list");
                    if(!pokemonList.contains(name + " - " + id)){
                        pokemonList.add(name + " - " + id);
                        adapter.notifyDataSetChanged();
                    }

                }
                catch(JSONException e){
                    Log.i("apiCall", "Runtime Exception");
                    Toast.makeText(getApplicationContext(), "Invalid Entry for Pokemon ", Toast.LENGTH_LONG).show();
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(ANError anError) {
                Toast.makeText(getApplicationContext(), "Invalid Entry for Pokemon ", Toast.LENGTH_LONG).show();
                Log.i("apiCall", anError.toString());
            }
        });
    }

    /**
     * Updates website displayed
     * @param url
     */
    public void updateWebsite(String url){

        Log.i("console", "Tried to update website to: " + url);
        WebView myWebview = (WebView) findViewById(R.id.webView);
        myWebview.getSettings().setJavaScriptEnabled(true);
        myWebview.getSettings().setDomStorageEnabled(true);
        myWebview.setWebViewClient(new WebViewClient());
        myWebview.loadUrl(url);

        myWebview.clearView();
        myWebview.measure(100, 100);
        myWebview.getSettings().setUseWideViewPort(true);
        myWebview.getSettings().setLoadWithOverviewMode(true);
    }


}