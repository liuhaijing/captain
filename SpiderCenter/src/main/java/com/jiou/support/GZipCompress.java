package com.jiou.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class GZipCompress {

	private static final String charset = "UTF-8";

	public static String compressToString(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		GZIPOutputStream gos = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			gos = new GZIPOutputStream(out);
			gos.write(s.getBytes(charset));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(gos);
		}
		return new BASE64Encoder().encode(out.toByteArray());
	}

	public static String uncompress(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPInputStream gis = null;
		byte[] compressed = null;
		String decompressed = null;
		try {
			compressed = new BASE64Decoder().decodeBuffer(s);
			ByteArrayInputStream in = new ByteArrayInputStream(compressed);
			gis = new GZIPInputStream(in);
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = gis.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(gis);
		}
		return decompressed;
	}

	public static byte[] compressToByteArray(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		GZIPOutputStream gos = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			gos = new GZIPOutputStream(out);
			gos.write(s.getBytes(charset));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(gos);
		}
		return out.toByteArray();
	}

	public static String uncompress(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPInputStream gis = null;
		String decompressed = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			gis = new GZIPInputStream(in);
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = gis.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(gis);
		}
		return decompressed;
	}

	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
