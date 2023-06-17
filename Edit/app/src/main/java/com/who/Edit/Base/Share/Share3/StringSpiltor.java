package com.who.Edit.Base.Share.Share3;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class StringSpiltor
{
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
