package apt.com.miniproject;

/**
 * Created by rongshengxu on 10/24/15.
 */
import android.app.Dialog;
import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import org.json.*;
import com.loopj.android.http.*;


public class ViewStreamSingle extends ActionBarActivity {

    Context context = this;
    private String TAG  = "Display Single Stream";
    private Context mycontext;
    private TextView stname;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream_single);
        Bundle bundle = getIntent().getExtras();
        message = bundle.getString("message");
        System.out.println("Stream is " + message);

        stname = (TextView) findViewById(R.id.name);
        stname.setText("View stream: " + message);

        Button uploadButton = (Button) findViewById(R.id.open_image_upload_page);
        uploadButton.setClickable(true);

        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ImageUpload.class);
                        intent.putExtra("message", message);
                        startActivity(intent);
                    }
                }
        );

        final String request_url = "http://sacred-highway-108321.appspot.com/android/mobileviewsingle=="+ message;
//        final String request_url = "http://aptandroiddemo.appspot.com/viewAllPhotos";

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(request_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                final ArrayList<String> imageURLs = new ArrayList<String>();
                final ArrayList<String> imageCaps = new ArrayList<String>();
                try {
                    JSONObject jObject = new JSONObject(new String(response));
                    JSONArray displayImages = jObject.getJSONArray("imageurl");
                    JSONArray displayCaption = jObject.getJSONArray("imagecap");
//                    JSONArray displayImages = jObject.getJSONArray("displayImages");
//                    JSONArray displayCaption = jObject.getJSONArray("imageCaptionList");
                    System.out.println(new String(response));
                    for (int i = 0; i < displayImages.length(); i++) {

                        imageURLs.add(displayImages.getString(i));
                        imageCaps.add(displayCaption.getString(i));
                        System.out.println(displayImages.getString(i));
                    }
                    System.out.println("Test.....");
                    GridView gridview = (GridView) findViewById(R.id.gridview);
                    gridview.setAdapter(new ImageAdapter(context,imageURLs));
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {

                            Toast.makeText(context, imageCaps.get(position), Toast.LENGTH_SHORT).show();

                            Dialog imageDialog = new Dialog(context);
                            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            imageDialog.setContentView(R.layout.thumbnail);
                            ImageView image = (ImageView) imageDialog.findViewById(R.id.thumbnail_IMAGEVIEW);

                            Picasso.with(context).load(imageURLs.get(position)).into(image);

                            imageDialog.show();
                        }
                    });
                }
                catch(JSONException j){
                    System.out.println("JSON Error");
                    System.out.println(new String(response));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

//    @Override
//    public void onConnected(Bundle connectionHint) {
//        // Reaching onConnected means we consider the user signed in.
//        Log.i(TAG, "onConnected");
//
//        // Update the user interface to reflect that the user is signed in.
////        mSignInButton.setEnabled(false);
////        mSignOutButton.setEnabled(true);
////        mRevokeButton.setEnabled(true);
//
//        // Retrieve some profile information to personalize our app for the user.
////        final Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
////        email = Plus.AccountApi.getAccountName(mGoogleApiClient);
////        System.out.println(email);
////        // Indicate that the sign in process is complete.
////        mSignInProgress = STATE_DEFAULT;
////
////
////        mStatus.setText(email + " is currently Signed In");
//
//        Button uploadButton = (Button) findViewById(R.id.open_image_upload_page);
//        uploadButton.setClickable(true);
//
//        uploadButton.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(context, ImageUpload.class);
//                        startActivity(intent);
//                    }
//                }
//        );
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_stream_single, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void viewStream(View view){
        Intent intent= new Intent(this, ViewStream.class);
        startActivity(intent);
    }

    public void showMore(View view) {
        Intent intent = new Intent(this, ViewStreamSingle.class);
        intent.putExtra("message", message);
        startActivity(intent);
    }

}
