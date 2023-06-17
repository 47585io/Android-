package com.who.Edit.Base.Share.Share3;

import java.io.*;
import java.net.*;
import java.util.regex.*;
import android.text.*;

public class StringSpiltor
{
	/* 查找字符串和查找字符效率完全不一样，
	   在String中查找字符串300000次花费30ms
	   在StringBuilder中查找字符串300000次花费60ms
	   
	   但如果是查找字符，基本上只要几ms
	   但貌似String已经考虑到了这点，如果字符串只有一个字符，String会转而调用字符的查找函数
	   注意，StringBuilder并没有考虑到这点！
	*/
	/* toString很消耗时间，当一串字符有1000000行*45个字符时，toString一次需要30ms!!!。这相当于indexOf使用300000次的时间
	   这里就谈谈，toString本质是拷贝了一份字符数组，并且是逐个字符地拷贝
	   特别是CharSequence，如果只要一个范围内的字符串，一定先subSequence，再toString，节省时间！！！
	   但有一个例外，不知道为什么，StringBuilder的toString很快。非常快
	*/
	/* StringBuilder的insert和delete效率非常慢，超级慢
	   当一串5000*15个字符的StringBuilder，插入5000次居然要250ms
	*/
	
	//字符c第n次出现的下标，从index开始
	public static int NIndex(char c,String text,int index,int n)
	{
		while(n-->0){
		    index = text.indexOf(c,index);
			if(index==-1||n<1)
				break;
			++index;
		}
		return index;
	}
	//字符c倒数第几次出现的下标，从index开始
	public static int lastNIndex(char c,String text,int index,int n)
	{
		while(n-->0){
		    index = text.lastIndexOf(c,index);
			if(index==-1||n<1)
				break;
			--index;
		}
		return index;
	}
	//字符c在start~end范围内出现的次数
	public static int Count(char want,String text,int index,int endIndex)
	{
		int count=0;
		while(true){
		    index = text.indexOf(want,index);
			if(index==-1||index>=endIndex)
				break;
			++count;
			++index;
		}
		return count;
	}

	/* 同上，这是对于String的重载 */
	public static int NIndex(String c,String text,int index,int n)
	{
		while(n-->0){
		    index = text.indexOf(c,index);
			if(index==-1||n<1)
				break;
			++index;
		}
		return index;
	}
	public static int lastNIndex(String c,String text,int index,int n)
	{
		while(n-->0){
		    index = text.lastIndexOf(c,index);
			if(index==-1||n<1)
				break;
			--index;
		}
		return index;
	}
	public static int Count(String want,String text,int index,int endIndex)
	{
		int count=0;
		while(true){
		    index = text.indexOf(want,index);
			if(index==-1||index>=endIndex)
				break;
			++count;
			++index;
		}
		return count;
	}

	/* 同上，这是对于StringBuilder的重载 */
	public static int NIndex(String str,StringBuilder text,int index,int n)
	{
		while(n-->0){
		    index = text.indexOf(str,index);
			if(index==-1||n<1)
				break;
			++index;
		}
		return index;
	}
	public static int lastNIndex(String str,StringBuilder text,int index,int n)
	{
		while(n-->0){
		
		    index = text.lastIndexOf(str,index);
			if(index==-1||n<1)
				break;
			--index;
		}
		return index;
	}
	public static int Count(String want,StringBuilder text,int index,int endIndex)
	{
		int count=0;
		while(true){
		    index = text.indexOf(want,index);
			if(index==-1||index>=endIndex)
				break;
			++count;
			++index;
		}
		return count;
	}
	
	/* 同上，这是对于StringBuffer的重载 */
	public static int NIndex(String str,StringBuffer text,int index,int n)
	{
		while(n-->0){
		    index = text.indexOf(str,index);
			if(index==-1||n<1)
				break;
			++index;
		}
		return index;
	}
	public static int lastNIndex(String str,StringBuffer text,int index,int n)
	{
		while(n-->0){

		    index = text.lastIndexOf(str,index);
			if(index==-1||n<1)
				break;
			--index;
		}
		return index;
	}
	public static int Count(String want,StringBuffer text,int index,int endIndex)
	{
		int count=0;
		while(true){
		    index = text.indexOf(want,index);
			if(index==-1||index>=endIndex)
				break;
			++count;
			++index;
		}
		return count;
	}
}
