/**
  * Generated by smali2java 1.0.0.558
  * Copyright (C) 2013 Hensence.com
  */

package dyhelper.com.weather;


class perWeather {
    String city;
    String date;
    String tem;
    String weather;
    String wind;
    
    perWeather(String date, String weather, String wind, String tem) {
        this.date = date;
        this.weather = weather;
        this.wind = wind;
        this.tem = tem;
    }
    
    perWeather(String city, String date, String weather, String wind, String tem) {
    	this.city = city;
    	this.date = date;
    	this.weather = weather;
    	this.wind = wind;
    	this.tem = tem;
    }
}
