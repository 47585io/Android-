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
	public static int NIndex(String text,char c,int n,int hasCount)
	{
		int index=0;
		if(n<hasCount/2)
		{
			while(n-->0){
				index = text.indexOf(c,index);
				if(index==-1||n<1)
					break;
				++index;
			}
		}
		else
		{
			n = hasCount-n;
			index = text.length()-1;
			while(n-->0){
				index = text.lastIndexOf(c,index);
				if(index==-1||n<1)
					break;
				--index;
			}
		}
		return index;
	}
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

}
