/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * MD5 Utils
 *
 * @author Z
 */
public class CommonMD5 {

	public static final String TAG = "MD5";

	/**
	 * 32 bit Lowercase
	 *
	 * @param val
	 * @return
	 */
	public static String getMD5(String val) {
		try {
			CommonLogUtil.d(TAG, val);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(val.getBytes());
			return HexEncode(md5.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 32 bit Capital
	 *
	 * @param val
	 * @return
	 */
	public static String getUPMD5(String val) {
		try {
			CommonLogUtil.d(TAG, val);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(val.getBytes());
			return UpHexEncode(md5.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String HexEncode(byte[] toencode) {
		StringBuilder sb = new StringBuilder(toencode.length * 2);
		for (byte b : toencode) {
			sb.append(Integer.toHexString((b & 0xf0) >>> 4));
			sb.append(Integer.toHexString(b & 0x0f));
		}
		return sb.toString().toLowerCase(Locale.US);
	}

	private static String UpHexEncode(byte[] toencode) {
		StringBuilder sb = new StringBuilder(toencode.length * 2);
		for (byte b : toencode) {
			sb.append(Integer.toHexString((b & 0xf0) >>> 4));
			sb.append(Integer.toHexString(b & 0x0f));
		}
		return sb.toString().toUpperCase(Locale.US);
	}


	/**
	 * @param input
	 * @return
	 */
	public static String getLowerMd5(String input) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(input.getBytes());
			return toHexString(md5.digest()).toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static String toHexString(byte[] b) throws Exception {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	private static char[] hexChar = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

}
