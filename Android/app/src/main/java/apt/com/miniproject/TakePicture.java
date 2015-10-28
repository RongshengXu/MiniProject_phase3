package apt.com.miniproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.GpsStatus;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TakePicture extends ActionBarActivity implements
        SurfaceHolder.Callback,PictureCallback, View.OnClickListener{
    private static final int TAKE_PICTURE = 2;

    Context context = this;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button takePhotoButton;
    private Camera mCamera;
    private boolean mPreviewRunning;
    private View.OnClickListener listener;
    private String mCurrentPhotoPath;
    private File photo;
    private ImageView mImageView;
    private Button submitButton;
    private String streamName;
    private byte[] mImage;
    private String user_name;

    byte[] image_data;
    private Bitmap m_bitmap;
//    protected MyApplication myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takepicture);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        streamName = extras.getString("stream_name");
        user_name = extras.getString("user");

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setVisibility(View.VISIBLE);
        listener = this;

        // To take a picture
        takePhotoButton = (Button) findViewById(R.id.Take_Pic_Button);
        takePhotoButton.setOnClickListener(listener);
        takePhotoButton.setVisibility(View.VISIBLE);
        takePhotoButton.setEnabled(true);
        takePhotoButton.setClickable(true);

        submitButton = (Button) findViewById(R.id.upload_to_server);
        submitButton.setOnClickListener(listener);
        submitButton.setEnabled(false);
        submitButton.setClickable(false);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);

        mPreviewRunning = true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Take_Pic_Button:
                //System.out.println("take_photo_button_clicked");
                mCamera.takePicture(null,null,this);
                break;
//            case R.id.submit_button:
//                Intent intent= new Intent(this, ImageUpload.class);
//                intent.putExtra("message", streamName);
//                intent.putExtra("user", user_name);
//                intent.putExtra("id", "camera");
//                intent.putExtra("data", image_data);
//                //startActivity(intent);
//                startActivityForResult(intent, TAKE_PICTURE);
                //uploadImage();
            case R.id.upload_to_server:
                uploadHandler(image_data, streamName);
                break;
        }
    }

    private void uploadHandler(final byte[] encodedImage, final String stream_name) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        String upload_url = "http://sacred-highway-108321.appspot.com/android/upload";
        RequestParams params = new RequestParams();
        params.put("file",new ByteArrayInputStream(encodedImage));
        params.put("streamname", stream_name);
        AsyncHttpClient client = new AsyncHttpClient();
        System.out.println(upload_url+" \n" + encodedImage.toString());
        client.post(upload_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.w("async", "success!!!!");
                Toast.makeText(context, "Upload Successful", Toast.LENGTH_SHORT).show();
                // go back to single stream view
                Intent intent= new Intent(context, ViewStreamSingle.class);
                intent.putExtra("message", stream_name);
                intent.putExtra("user", user_name);
                startActivity(intent);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                System.out.println(new String(errorResponse));
                Log.e("Posting_to_blob", "There was a problem in retrieving the url : " + e.toString());
            }
        });
//            
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mPreviewRunning){
            mCamera.stopPreview();
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        // Choose the most appropriate previewSize for the app
        Camera.Size previewSize = previewSizes.get(0);

        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
        mPreviewRunning = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data != null){
            submitButton.setClickable(true);
            submitButton.setEnabled(true);

            image_data = data;
            m_bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            Matrix matrix = new Matrix();
            // setup rotation degree
            matrix.postRotate(90);

            m_bitmap = Bitmap.createBitmap(m_bitmap, 0, 0, m_bitmap.getWidth(), m_bitmap.getHeight(), matrix, true);

            if (m_bitmap != null){
                mImageView.setImageBitmap(m_bitmap);
            }
        }
        mCamera.startPreview();
    }
}
