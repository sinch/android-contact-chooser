package com.sinch.contactchooser;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChooseContact extends ActionBarActivity {

    public final int PICK_CONTACT = 2015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);

        (findViewById(R.id.button)).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            (new normalizePhoneNumberTask()).execute(cursor.getString(column));
        }
    }

    class normalizePhoneNumberTask extends AsyncTask<String, Void, String> {

        private String appKey = "your_app_key";
        private String appSecret = "your_app_secret";

        @Override
        protected String doInBackground(String... params) {

            String normalizedPhoneNumber = "";

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("https://callingapi.sinch.com/v1/calling/query/number/" + params[0].replaceAll("\\s+",""));

                String usernamePassword = "application:" + appKey + ":" + appSecret;
                String encoded = Base64.encodeToString(usernamePassword.getBytes(), Base64.NO_WRAP);
                httpGet.addHeader("Authorization", "Basic " + encoded);

                HttpResponse response = httpclient.execute(httpGet);
                ResponseHandler<String> handler = new BasicResponseHandler();
                normalizedPhoneNumber = parseJSONResponse(handler.handleResponse(response));
            } catch (ClientProtocolException e) {
                Log.d("ClientProtocolException", e.getMessage());
            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
            }

            return normalizedPhoneNumber;
        }

        @Override
        protected void onPostExecute(String normalizedPhoneNumber) {
            Toast.makeText(getApplicationContext(), normalizedPhoneNumber, Toast.LENGTH_LONG).show();
        }

        private String parseJSONResponse(String jsonString) {

            String returnString = "";

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                returnString = jsonObject.getJSONObject("number").getString("normalizedNumber");
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

            return returnString;
        }
    }
}
