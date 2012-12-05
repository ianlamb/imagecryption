package com.example.imagecryption;

/**
 * 	@file		MainActivity.java
 *	@author  	Ian Lamb
 *	@author		Derek Brown
 *	@version 	2.0
 *	@created 	Dec 04, 2012
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	ImageView thumbnail;
	TextView filename;
	EditText secret;
	Bitmap bmpImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        thumbnail = (ImageView) findViewById(R.id.imgThumbnail);
        filename = (TextView) findViewById(R.id.tvFilename);
        secret = (EditText) findViewById(R.id.txtSecret);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
    // browse for image files
	public void onBrowseClicked(View v) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 1337);
	}
	
	// encrypt the message into the selected image
	public void onSetClicked(View v) {
		if(bmpImage == null) {
			Toast.makeText(getBaseContext(), "No image selected, cannot encode", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		if(secret.getText().length() < 1) {
			Toast.makeText(getBaseContext(), "No secret message, cannot encode", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		bmpImage = Steganography.encode(bmpImage, secret.getText().toString());
		
		String path = Environment.getExternalStorageDirectory().toString();
		OutputStream fOut = null;
				
		File file = new File(path, filename.getText().toString());
		try{
			fOut = new FileOutputStream(file);
	
			bmpImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
			
			secret.setText("");
			Toast.makeText(getBaseContext(), "Secret encoded successfully and image saved", 
					Toast.LENGTH_SHORT).show();
	
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	
	// decrypt a message out of the selected image
	public void onGetClicked(View v) {
		if(bmpImage == null) {
			Toast.makeText(getBaseContext(), "No image selected, cannot decode", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		String decodedMsg = Steganography.decode(bmpImage);
		if(decodedMsg == null || decodedMsg.length() < 1) {
			Toast.makeText(getBaseContext(), "There was no message encoded in the image", 
					Toast.LENGTH_SHORT).show();
			return;
		} else {
			secret.setText(decodedMsg);
			Toast.makeText(getBaseContext(), "Secret decrypted from image!", 
					Toast.LENGTH_SHORT).show();
		}
	}
    
	// image picker intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == 1337) {
    		if(resultCode == Activity.RESULT_OK){
    			Uri selectedImage = data.getData();
    			thumbnail.setImageURI(selectedImage);

    			String sFilePath = getRealPathFromURI(selectedImage);
    			
    			if(sFilePath == null) {
    				Toast.makeText(getBaseContext(), "Image not found", 
    						Toast.LENGTH_SHORT).show();
    				return;
    			} else {
        			String saPathParts[] = sFilePath.split("/");
        			String sFileName = saPathParts[saPathParts.length-1];
        			
        			filename.setText(sFileName);
        			
    				bmpImage = BitmapFactory.decodeFile(sFilePath);
    				if(bmpImage == null) {
    					Toast.makeText(getBaseContext(), "Image invalid", 
    							Toast.LENGTH_SHORT).show();
    					return;
    				}
    			}
    		}
    	}
    }
    
    // gets the actual file path from a Uri object
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null); 
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
        return cursor.getString(idx); 
    }
}
