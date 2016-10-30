package com.jiou.coding;

import java.text.MessageFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhe.li
 */
@SuppressWarnings("unused")
public class Coding {
	private static final Logger logger = LoggerFactory.getLogger(Coding.class);

	private static final Random random = new Random();

	/**
	 * 
	 * @param bytes
	 *            binary array of captcha image.
	 * @return checkcode
	 */
	public static final String coding(byte[] bytes) {
		long start = System.currentTimeMillis();
		String checkcode = null;
		checkcode = byruokuai(bytes);
		long end = System.currentTimeMillis();
		logger.info("====>Get checkcode takes:{}ms;checkcode:{}", end - start, checkcode);
		return checkcode;
	}

	/**
	 * 
	 * @param imgPath
	 *            captcha image filepath on the local file system.
	 * @return checkcode
	 */
	public static final String coding(String imgPath) {
		long start = System.currentTimeMillis();
		String checkcode = null;
		checkcode = byruokuai(imgPath);
		long end = System.currentTimeMillis();
		logger.info("====>Get checkcode takes:{}ms;checkcode:{}", end - start, checkcode);
		return checkcode;
	}

	private static final String byruokuai(String imgPath) {
		String str = RKInter.createByPost("jimeng2016", "jimeng123", "3040", "60000", "46576",
				"62371295aea54dde895dc19c06d8b463", imgPath);
		String code = JSONObject.parseObject(str).getString("Result");
		return code;
	}

	private static final String byruokuai(byte[] bytes) {
		String str = RKInter.createByPost("jimeng2016", "jimeng123", "3040", "60000", "46576",
				"62371295aea54dde895dc19c06d8b463", bytes);
		String code = JSONObject.parseObject(str).getString("Result");
		return code;
	}

	public static void main(String[] args) {
		String code = coding("d:/seccode.jpg");
		System.out.println(code);
	}

}
