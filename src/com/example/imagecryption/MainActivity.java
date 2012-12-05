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

public class MainActivity extends Activity {
	
	ImageView thumbnail;
	TextView filename;
	EditText secret;
	Bitmap bmpImage;
	
	public void onBrowseClicked(View v) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 1337);
	}
	
	public void onSetClicked(View v) {
		bmpImage = Steganography.encode(bmpImage, secret.getText().toString());
		
		String path = Environment.getExternalStorageDirectory().toString();
		OutputStream fOut = null;
		File file = new File(path, "test.png");
		try{
			fOut = new FileOutputStream(file);
	
			bmpImage.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
	
			MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
		}catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	
	public void onGetClicked(View v) {
		secret.setText(Steganography.decode(bmpImage));
	}

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
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == 1337) {
    		if(resultCode == Activity.RESULT_OK){
    			Uri selectedImage = data.getData();
    			thumbnail.setImageURI(selectedImage);
    			
    			String sFile = getRealPathFromURI(selectedImage);
    			
    			filename.setText(sFile);
    			
    			if(sFile != null)
    				bmpImage = BitmapFactory.decodeFile(sFile);
    		}
    	}
    }
    
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null); 
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
        return cursor.getString(idx); 
    }
}
