package com.jiou.domain;

import java.io.Serializable;
import java.util.Date;

public class SinaSpread implements Serializable, Cloneable {

	private static final long serialVersionUID = 467005275190547533L;

	private String uid;
	private String query;// 搜索关键词
	private String puid;// 上级uid

	private String uname;// 博主名
	private int gender = 3;// 性别,1-男,2-女,3-未知
	private String brief;// 简介
	private String address;// 地址
	private String label;// 标签
	private Boolean isveri;// 是否加V
	private Boolean isvip;// 是否微博会员
	private Integer concern;// 关注数
	private Integer fans;// 粉丝数
	private Integer blognums;// 微博数
	private Integer level;// 微博等级

	private Date pubtime;// 发布时间
	private Integer reviews;// 评论数
	private Integer forwards;// 转发数
	private Integer likenum;// 点赞数
	private String content;// 正文

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		this.puid = puid;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getBrief() {
		return brief;
	}

	public void setBrief(String brief) {
		this.brief = brief;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean getIsveri() {
		return isveri;
	}

	public void setIsveri(Boolean isveri) {
		this.isveri = isveri;
	}

	public Boolean getIsvip() {
		return isvip;
	}

	public void setIsvip(Boolean isvip) {
		this.isvip = isvip;
	}

	public Integer getConcern() {
		return concern;
	}

	public void setConcern(Integer concern) {
		this.concern = concern;
	}

	public Integer getFans() {
		return fans;
	}

	public void setFans(Integer fans) {
		this.fans = fans;
	}

	public Integer getBlognums() {
		return blognums;
	}

	public void setBlognums(Integer blognums) {
		this.blognums = blognums;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Date getPubtime() {
		return pubtime;
	}

	public void setPubtime(Date pubtime) {
		this.pubtime = pubtime;
	}

	public Integer getReviews() {
		return reviews;
	}

	public void setReviews(Integer reviews) {
		this.reviews = reviews;
	}

	public Integer getForwards() {
		return forwards;
	}

	public void setForwards(Integer forwards) {
		this.forwards = forwards;
	}

	public Integer getLikenum() {
		return likenum;
	}

	public void setLikenum(Integer likenum) {
		this.likenum = likenum;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public SinaSpread clone() {
		try {
			return (SinaSpread) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "SinaSpread [uid=" + uid + ", query=" + query + ", puid=" + puid + ", uname=" + uname + ", gender="
				+ gender + ", brief=" + brief + ", address=" + address + ", label=" + label + ", isveri=" + isveri
				+ ", isvip=" + isvip + ", concern=" + concern + ", fans=" + fans + ", blognums=" + blognums
				+ ", level=" + level + ", pubtime=" + pubtime + ", reviews=" + reviews + ", forwards=" + forwards
				+ ", likenum=" + likenum + ", content=" + content + "]";
	}

}
