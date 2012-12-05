package com.example.imagecryption;

/**
 * Copyright (C) 2009  Pasquale Paola

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.util.Vector;

import android.util.Log;

/**
 * Core algorithm of MobiStego.
 * @author <a href="mailto:pasquale.paola@gmail.com">Pasquale Paola</a>
 * 
 */
public class LSB2bit {

	private static int[] binary = { 16, 8, 0 };	
	private static byte[] andByte = { (byte) 0xC0, 0x30, 0x0C, 0x03 };
	private static int[] toShift = { 6, 4, 2, 0 };
	public static String END_MESSAGE_COSTANT = "#!@";
	public static String START_MESSAGE_COSTANT = "@!#";

	/**
	 * This method represent the core of LSB on 2 bit (Encoding).
	 * @param oneDPix The <b>rgb</b> array.
	 * @param imgCols Image width.
	 * @param imgRows Image height.
	 * @param str Message to encode.
	 * @param hand A handler interface, for the progress bar.
	 * @return Encoded message image.
	 */
	public static byte[] encodeMessage(int[] oneDPix, int imgCols, int imgRows,
			String str) {
		str += END_MESSAGE_COSTANT;
		str = START_MESSAGE_COSTANT + str;
		byte[] msg = str.getBytes();
		int channels = 3;
		int shiftIndex = 4;
		//Array.newInstance(Byte.class, imgRows * imgCols * channels);
		byte[] result = new byte[imgRows * imgCols * channels];
		
		int msgIndex = 0;
		int resultIndex = 0;
		boolean msgEnded = false;
		for (int row = 0; row < imgRows; row++) {
			for (int col = 0; col < imgCols; col++) {
				int element = row * imgCols + col;
				byte tmp = 0;

				for (int channelIndex = 0; channelIndex < channels; channelIndex++) {
					if (!msgEnded) {
						tmp = (byte) ((((oneDPix[element] >> binary[channelIndex]) & 0xFF) & 0xFC) | ((msg[msgIndex] >> toShift[(shiftIndex++)
								% toShift.length]) & 0x3));// 6
						if (shiftIndex % toShift.length == 0) {
							msgIndex++;
						}
						if (msgIndex == msg.length) {
							msgEnded = true;
						}
					} else {
						tmp = (byte) ((((oneDPix[element] >> binary[channelIndex]) & 0xFF)));
					}
					result[resultIndex++] = tmp;
				}

			}

		}
		return result;

	}

	/**
	 * This is the decoding method of LSB on 2 bit.
	 * @param oneDPix The byte array image.
	 * @param imgCols Image width.
	 * @param imgRows Image height.
	 * @return The decoded message.
	 */
	public static String decodeMessage(byte[] oneDPix, int imgCols,
			int imgRows) {

		Vector<Byte> v = new Vector<Byte>();

		String builder = "";
		int shiftIndex = 4;
		byte tmp = 0x00;
		for (int i = 0; i < oneDPix.length; i++) {
			tmp = (byte) (tmp | ((oneDPix[i] << toShift[shiftIndex
					% toShift.length]) & andByte[shiftIndex++ % toShift.length]));
			if (shiftIndex % toShift.length == 0) {
				v.addElement(new Byte(tmp));
				byte[] nonso = { (v.elementAt(v.size() - 1)).byteValue() };
				String str = new String(nonso);
				// if (END_MESSAGE_COSTANT.equals(str)) {
				if (builder.endsWith(END_MESSAGE_COSTANT)) {
					break;
				} else {
					builder = builder + str;
					if (builder.length() == START_MESSAGE_COSTANT.length()
							&& !START_MESSAGE_COSTANT.equals(builder)) {
						builder = null;
						break;
					}
				}

				tmp = 0x00;
			}

		}
		if (builder != null)
			builder = builder.substring(START_MESSAGE_COSTANT.length(), builder
					.length()
					- END_MESSAGE_COSTANT.length());
		return builder;

	}
	
	/**
	 * Convert the byte array to an int array.
	 * @param b The byte array.
	 * @return The int array.
	 */

	public static int[] byteArrayToIntArray(byte[] b) {
		Log.v("Size byte array", b.length+"");
		int size=b.length / 3;
		Log.v("Size Int array",size+"");
		System.runFinalization();
		System.gc();
		Log.v("FreeMemory", Runtime.getRuntime().freeMemory()+"");
		int[] result = new int[size];
		int off = 0;
		int index = 0;
		while (off < b.length) {
			result[index++] = byteArrayToInt(b, off);
			off = off + 3;
		}

		return result;
	}

	/**
	 * Convert the byte array to an int.
	 * 
	 * @param b
	 *            The byte array
	 * @return The integer
	 */
	public static int byteArrayToInt(byte[] b) {
		return byteArrayToInt(b, 0);
	}

	/**
	 * Convert the byte array to an int starting from the given offset.
	 * 
	 * @param b
	 *            The byte array
	 * @param offset
	 *            The array offset
	 * @return The integer
	 */
	public static int byteArrayToInt(byte[] b, int offset) {
		int value = 0x00000000;
		for (int i = 0; i < 3; i++) {
			int shift = (3 - 1 - i) * 8;
			value |= (b[i + offset] & 0x000000FF) << shift;
		}
		value = value & 0x00FFFFFF;
		return value;
	}

	/**
	 * Convert integer array representing [argb] values to byte array
	 * representing [rgb] values
	 * 
	 * @param array Integer array representing [argb] values.
	 * @return byte Array representing [rgb] values.
	 */

	public static byte[] convertArray(int[] array) {
		byte[] newarray = new byte[array.length * 3];

		for (int i = 0; i < array.length; i++) {

			/*
			 * newarray[i * 3] = (byte) ((array[i]) & 0xFF); newarray[i * 3 + 1]
			 * = (byte)((array[i] >> 8)& 0xFF); newarray[i * 3 + 2] =
			 * (byte)((array[i] >> 16)& 0xFF);
			 */

			newarray[i * 3] = (byte) ((array[i] >> 16) & 0xFF);
			newarray[i * 3 + 1] = (byte) ((array[i] >> 8) & 0xFF);
			newarray[i * 3 + 2] = (byte) ((array[i]) & 0xFF);

		}
		return newarray;
	}

}