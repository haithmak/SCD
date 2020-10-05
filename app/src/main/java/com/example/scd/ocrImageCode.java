package com.example.scd;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Maqtari on 08/11/2018.
 */

public class ocrImageCode   {
    private final Context mContext ;

    private static final String LOG_TAG = "Text API";

    public  Uri imageUri;
    private TextRecognizer detector;
    public  static String mCurrentPhotoPath;

    public ocrImageCode (Context context) {
        this.mContext = context;

    }

    public File takePicture(Intent takePictureIntent) {

        // takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
                Toast.makeText(mContext, "path " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT)
                        .show();


            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(mContext, "Failed to create Image", Toast.LENGTH_SHORT)
                        .show();

            }
        }

        return photoFile ;
    }


    /*
 soluation in the link
 https://stackoverflow.com/questions/42516126/fileprovider-illegalargumentexception-failed-to-find-configured-root
     UPDATE 2020 MAR 13

     Provider path for a specific path as followings:

     <files-path/> --> Context.getFilesDir()
     <cache-path/> --> Context.getCacheDir()
     <external-path/> --> Environment.getExternalStorageDirectory()
     <external-files-path/> --> Context.getExternalFilesDir(String)
     <external-cache-path/> --> Context.getExternalCacheDir()
     <external-media-path/> --> Context.getExternalMediaDirs()
     Ref: https://developer.android.com/reference/androidx/core/content/FileProvider
 */

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //   File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //  File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DCIM);

        File storageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
        if (!storageDir.exists())
            storageDir.mkdirs();


        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }




    public String launchMediaScanIntent(Intent data , ImageView imageview ) {
        String blocks = "";
        String lines = "";
        String words = "";
        try {

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            imageUri = data.getData() ;
            mediaScanIntent.setData(imageUri);
            mContext.sendBroadcast(mediaScanIntent);

            Bitmap bitmap = decodeBitmapUri( mContext, imageUri , imageview );
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
             Log.e(LOG_TAG, e.toString());
        }


        return words ;

    }


    public Bitmap decodeBitmapUri(Context ctx, Uri uri ,ImageView imageview) throws FileNotFoundException
    {
        // int targetW = 600;
        // int targetH = 600;
        // Get the dimensions of the View
        int targetW = imageview.getWidth()  ;
        int targetH = imageview.getHeight() ;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

      //  int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
    }






    public static final String insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
             //   storeThumbnail(cr, miniThumb, id, 50F, 50F,Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }



}
