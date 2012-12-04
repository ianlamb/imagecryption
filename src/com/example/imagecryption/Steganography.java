package com.example.imagecryption;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * 	@file		Steganography.java
 *	@author  	Ian Lamb
 *	@author		Derek Brown
 *	@version 	2.0
 *	@created 	Dec 04, 2012
 */

public class Steganography
{
	
	public Steganography() {}

	// encodes secret to bitmap
	public static Bitmap encode(Bitmap bmp, String secret)
	{
		//convert all items to byte arrays: image, message, message length
		Bitmap newImage = null;
		byte img[]  = getByteData(bmp);
		byte msg[] = secret.getBytes();
		byte len[]   = bitConversion(msg.length);
		try
		{
			encodeText(img, len,  0); //0 first positiong
			encodeText(img, msg, 32); //4 bytes of space for length: 4bytes*8bit = 32 bits
			newImage = bytesToBitmap(img);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return newImage;
	}
	
	// decodes secret from bitmap
	public static String decode(Bitmap bmp)
	{
		String secret = "";
		byte[] decode;
		try
		{
			decode = decodeText(getByteData(bmp));
			secret = new String(decode);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return secret;
	}
	
	// get byte data from bitmap
	private static byte[] getByteData(Bitmap bmp)
	{	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] bytes = stream.toByteArray();
		
		return bytes;
	}

	// get bitmap from byte data
	private static Bitmap bytesToBitmap(byte[] bytes)
	{
		Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		
		return bmp;
	}
	
	// convert int to byte array
	private static byte[] bitConversion(int i)
	{
		//originally integers (ints) cast into bytes
		//byte byte7 = (byte)((i & 0xFF00000000000000L) >>> 56);
		//byte byte6 = (byte)((i & 0x00FF000000000000L) >>> 48);
		//byte byte5 = (byte)((i & 0x0000FF0000000000L) >>> 40);
		//byte byte4 = (byte)((i & 0x000000FF00000000L) >>> 32);
		
		//only using 4 bytes
		byte byte3 = (byte)((i & 0xFF000000) >>> 24); //0
		byte byte2 = (byte)((i & 0x00FF0000) >>> 16); //0
		byte byte1 = (byte)((i & 0x0000FF00) >>> 8 ); //0
		byte byte0 = (byte)((i & 0x000000FF)	   );
		//{0,0,0,byte0} is equivalent, since all shifts >=8 will be 0
		return(new byte[]{byte3,byte2,byte1,byte0});
	}
	
	// encodes the text
	private static byte[] encodeText(byte[] image, byte[] addition, int offset)
	{
		//check that the data + offset will fit in the image
		if(addition.length + offset > image.length)
		{
			throw new IllegalArgumentException("File not long enough!");
		}
		//loop through each addition byte
		for(int i=0; i<addition.length; ++i)
		{
			//loop through the 8 bits of each byte
			int add = addition[i];
			for(int bit=7; bit>=0; --bit, ++offset) //ensure the new offset value carries on through both loops
			{
				//assign an integer to b, shifted by bit spaces AND 1
				//a single bit of the current byte
				int b = (add >>> bit) & 1;
				//assign the bit by taking: [(previous byte value) AND 0xfe] OR bit to add
				//changes the last bit of the byte in the image to be the bit of addition
				image[offset] = (byte)((image[offset] & 0xFE) | b );
			}
		}
		return image;
	}
	
	// decodes the text
	private static byte[] decodeText(byte[] image)
	{
		int length = 0;
		int offset  = 32;
		//loop through 32 bytes of data to determine text length
		for(int i=0; i<32; ++i) //i=24 will also work, as only the 4th byte contains real data
		{
			length = (length << 1) | (image[i] & 1);
		}
		
		byte[] result = new byte[length];
		
		//loop through each byte of text
		for(int b=0; b<result.length; ++b )
		{
			//loop through each bit within a byte of text
			for(int i=0; i<8; ++i, ++offset)
			{
				//assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
				result[b] = (byte)((result[b] << 1) | (image[offset] & 1));
			}
		}
		return result;
	}
}

