# Use Android Contact Picker to Make Phone Calls
In this tutorial, I will walk you through using the Android Contact Picker to let your users choose a phone number from their contact book. Upon finishing this tutorial, you can follow our [Android calling tutorial](https://www.sinch.com/tutorials/app-to-phone-calling-android/) to make a call with Sinch.

If you get stuck at any point, you can check out the finished source code on our [GitHub](http://www.github.com/sinch/android-contact-chooser).

## Setup
If you haven't already, [sign up for a Sinch account](https://www.sinch.com/dashboard/#/signup). Create a new app and take note of the key and secret.

## User interface
The user interface is as simple as can be—just a button!

    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingLeft="@dimen/activity_horizontal_margin"
        android:paddingRight="@dimen/activity_horizontal_margin"
        android:paddingTop="@dimen/activity_vertical_margin"
        android:paddingBottom="@dimen/activity_vertical_margin"
        tools:context=".ChooseContact">

        <Button
            android:layout_width="fill_parent"
            android:layout_height="wrap_content"
            android:text="Choose contact"
            android:id="@+id/button"
            android:gravity="center"
            android:layout_centerVertical="true"/>
    </RelativeLayout>

Users will click this button to make the contact picker pop up.

## Choose contact

Move back over to your main activity java file. In onCreate, set an onClickListener for the button. When the button is clicked, you want to create an intent that opens the contact book and returns a result when a user is clicked.

    //declare globally, this can be any int
    public final int PICK_CONTACT = 2015;

    //onCreate
    (findViewById(R.id.button)).setOnClickListener( new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(i, PICK_CONTACT);
        }
    });

After the user has chosen a contact, you need to parse the result returned from startActivityForResult for a phone number.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            Log.d("phone number", cursor.getString(column));
        }
    }

If you run the app now, it will print the chosen phone number in your logs.

## Normalize phone number

To ready this phone number to be used to make a call with Sinch, you will use an AsyncTask to make a call to our phone number normalization API. See the comments below for an explanation of what's happening:

    class normalizePhoneNumberTask extends AsyncTask<String, Void, String> {

        //input your app key and secret from the Sinch dashboard
        private String appKey = "your_app_key";
        private String appSecret = "your_app_secret";

         //takes phone number string as an argument
         @Override
         protected String doInBackground(String... params) {

             String normalizedPhoneNumber = "";

             try {
                 //get ready to make a get request to normalize the phone number
                 HttpClient httpclient = new DefaultHttpClient();
                 HttpGet httpGet = new HttpGet("https://callingapi.sinch.com/v1/calling/query/number/" + params[0].replaceAll("\\s+",""));

                 //sinch uses basic authentication
                 String usernamePassword = "application:" + appKey + ":" + appSecret;
                 String encoded = Base64.encodeToString(usernamePassword.getBytes(), Base64.NO_WRAP);
                 httpGet.addHeader("Authorization", "Basic " + encoded);

                 //handle the response
                 HttpResponse response = httpclient.execute(httpGet);
                 ResponseHandler<String> handler = new BasicResponseHandler();

                 //parse JSON response from Sinch to get phone number
                 normalizedPhoneNumber = parseJSONResponse(handler.handleResponse(response));
             } catch (ClientProtocolException e) {
                 Log.d("ClientProtocolException", e.getMessage());
             } catch (IOException e) {
                 Log.d("IOException", e.getMessage());
             }

             return normalizedPhoneNumber;
         }

         //once the asynctask is complete, display a toast message with the normalized phone number
         @Override
         protected void onPostExecute(String normalizedPhoneNumber) {
             //if you want to make a call with sinch, this is the place to do it!
             Toast.makeText(getApplicationContext(), normalizedPhoneNumber, Toast.LENGTH_LONG).show();
         }

         //the sinch api returns a json like {"number":{"restricted":false,"countryId":"US","numberType":"Mobile","normalizedNumber":"+16507141052"}}
         //this method will return a string of just the phone number, +16507141052
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

Now, you can run the app, choose a contact, and a toast message of the normalized number will pop up.

I hope this gives a good example of how to use the Android Contact Picker. Next up, we will be using Sinch to make a phone call with that number. Head over to our [Android tutorial](https://www.sinch.com/tutorials/app-to-phone-calling-android/).
