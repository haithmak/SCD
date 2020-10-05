package com.example.scd;



import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    SurfaceView mCameraView;
    TextView mTextView;
    CameraSource mCameraSource;
    Button mCamera  , mGallary , mDecode;
    ocrImageCode de;
    ImageView imageview ;
    private static final String TAG = "MainActivity";
    private Uri imageUri;

    private static final int requestPermissionID = 101;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextView = findViewById(R.id.text_view);
        mCamera= findViewById(R.id.camera);
        mGallary= findViewById(R.id.Gallery);
        imageview = findViewById(R.id.image_view);

        de=new ocrImageCode( this );

        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
             mTextView.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }
        mGallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent p = new Intent();
                p.setType( "image/*" );
                p.setAction( Intent.ACTION_GET_CONTENT );
                startActivityForResult( p , REQUEST_IMAGE_CAPTURE );
            }
        });


        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED){
                        //permission not enabled, request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else {
                        //permission already granted
                        openCamera();
                    }
                }
                else {
                    //system os < marshmallow
                    openCamera();
                }

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                openCamera();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }




    private void openCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = de.takePicture(takePictureIntent) ;
        try {
            // Continue only if the File was successfully created
            if (photoFile != null) {

                imageUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);


                Log.e("Main imageUri" , imageUri.toString()) ;
                //  imageUri = de.imageUri ;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        } catch (Exception e) {
            Log.e("Main" , e.toString()) ;
            Toast.makeText(this, e. toString() +"  Failed to load Image ", Toast.LENGTH_SHORT)
                    .show();


        }
    }





    String des ="" ;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //called when image was captured from camera

        try {
            //Toast.makeText(v.getContext(), "requestCode=" +requestCode + "  resultCode=" + resultCode, Toast.LENGTH_LONG).show();

            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK ) {
                // Show the thumbnail on ImageView
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageview.setImageBitmap(imageBitmap);
                imageUri = Uri.parse(ocrImageCode.mCurrentPhotoPath);

                File file = new File( imageUri.getPath() );
                try {
                    InputStream ims = new FileInputStream(file);
                  //  imageview.setImageBitmap(BitmapFactory.decodeStream(ims));
                 //   imageview.setImageBitmap(imageBitmap);
                   // imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    // ScanFile so it will be appeared on Gallery
                    MediaScannerConnection.scanFile(this,
                            new String[]{imageUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    des=de.launchMediaScanIntent(data ,imageview ) ;

                                    mTextView.setText(des);
                                }


                            });

                } catch (FileNotFoundException e) {
                    Log.e("ocr imageUri" , e.toString()) ;
                }



            }

            else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            {
                des=de.launchMediaScanIntent(data ,imageview ) ;

                Toast.makeText(this, des , Toast.LENGTH_LONG).show();
            }

            imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mTextView.setText(des);
         //   Toast.makeText(this, des , Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                    .show();
               Log.e("main", e.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, mTextView.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }
}