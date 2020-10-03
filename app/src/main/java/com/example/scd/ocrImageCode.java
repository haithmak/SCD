package com.example.scd;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Maqtari on 08/11/2018.
 */

public class ocrImageCode   {
    private final Context mContext ;

    private static final String LOG_TAG = "Text API";

    private Uri imageUri;
    private TextRecognizer detector;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;

    public ocrImageCode (Context context) {
        this.mContext = context;



    }



    String des ="";
    public String onActivityResult(int requestCode, int resultCode, final Intent data , final ImageView imageview ) {

        try {
            //Toast.makeText(v.getContext(), "requestCode=" +requestCode + "  resultCode=" + resultCode, Toast.LENGTH_LONG).show();

            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK ) {
                // Show the thumbnail on ImageView
                imageUri = Uri.parse(mCurrentPhotoPath);

                File file = new File(imageUri.getPath());
                try {
                    InputStream ims = new FileInputStream(file);
                    imageview.setImageBitmap( BitmapFactory.decodeStream(ims));
                } catch (FileNotFoundException e) {

                }

                // ScanFile so it will be appeared on Gallery
                MediaScannerConnection.scanFile(mContext,
                        new String[]{imageUri.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                des=launchMediaScanIntent(data ,imageview) ;
                            }
                        });
            }

            else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            {
               des=launchMediaScanIntent(data ,imageview) ;
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Failed to load", Toast.LENGTH_SHORT)
                    .show();
            //   Log.e(LOG_TAG, e.toString());
        }

        return des ;
    }

    private String launchMediaScanIntent(Intent data ,ImageView imageview) {
        String blocks = "";
        String lines = "";
        String words = "";
        try {

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(imageUri);
            mContext.sendBroadcast(mediaScanIntent);

            imageUri = data.getData();

            Bitmap bitmap = decodeBitmapUri(mContext, imageUri , imageview);

            imageview.setImageBitmap(bitmap);
            detector = new TextRecognizer.Builder(mContext).build();

            if (detector.isOperational() && bitmap != null) {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);
                 blocks = "";
                 lines = "";
                 words = "";


                for (int index = 0; index < textBlocks.size(); index++) {
                    //extract scanned text blocks here
                    TextBlock tBlock = textBlocks.valueAt(index);
                    blocks = blocks + tBlock.getValue() + "\n" + "\n";
                    for (Text line : tBlock.getComponents()) {
                        //extract scanned text lines here
                        lines = lines + line.getValue() + "\n";

                        for (Text element : line.getComponents()) {
                            //extract scanned text words here
                            words = words + element.getValue() + ",";



                        }
                    }
                }


                if (textBlocks.size() == 0 )
                {
                    //   scanResults.setText("Scan Failed: Found nothing to scan");

                    Toast.makeText(mContext, "Scan Failed: Found nothing to Decode", Toast.LENGTH_LONG).show();
                }
                else
                {
             //       Toast.makeText(mContext, lines, Toast.LENGTH_LONG).show();
                                       /*
                    scanResults.setText(scanResults.getText() + "Blocks: " + "\n");
                    scanResults.setText(scanResults.getText() + blocks + "\n");
                    scanResults.setText(scanResults.getText() + "---------" + "\n");
                    scanResults.setText(scanResults.getText() + "Lines: " + "\n");
                    scanResults.setText(scanResults.getText() + lines + "\n");
                    scanResults.setText(scanResults.getText() + "---------" + "\n");
                    scanResults.setText(scanResults.getText() + "Words: " + "\n");
                    scanResults.setText(scanResults.getText() + words + "\n");
                    scanResults.setText(scanResults.getText() + "---------" + "\n");
                        */

                }
            }
            else {
                Toast.makeText(mContext, "Could not set up the detector!", Toast.LENGTH_LONG)
                        .show();
                //  scanResults.setText("Could not set up the detector!");
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Failed to load Image", Toast.LENGTH_SHORT).show();
            //   Log.e(LOG_TAG, e.toString());
        }


        return words ;

    }


    private Bitmap decodeBitmapUri(Context ctx, Uri uri ,ImageView imageview) throws FileNotFoundException
    {
        // int targetW = 600;
        // int targetH = 600;
        // Get the dimensions of the View
        int targetW = imageview.getWidth() /2 ;
        int targetH = imageview.getHeight() /2 ;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
    }



    public File takePicture(Intent takePictureIntent) {

       // takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(mContext, "Failed to create Image", Toast.LENGTH_SHORT)
                        .show();

            }
            }

        return photoFile ;
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }




}
