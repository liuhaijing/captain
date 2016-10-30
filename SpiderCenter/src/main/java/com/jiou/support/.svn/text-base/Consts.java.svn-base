package com.jiou.support;

import java.util.ResourceBundle;

/**
 * @author zhe.li
 */
public class Consts {

	public static final String jdbc_url;
	public static final String jdbc_user;
	public static final String jdbc_pwd;

	static {
		ResourceBundle rb = ResourceBundle.getBundle("config");
		jdbc_url = rb.getString("jdbc.url");
		jdbc_user = rb.getString("jdbc.user");
		jdbc_pwd = rb.getString("jdbc.password");
	}

	public static final String mongo_database_name = "spider";
	public static final String tmall_product_collection_name = "tmall_product";
	public static final String tmall_comment_collection_name = "tmall_comment";

}
