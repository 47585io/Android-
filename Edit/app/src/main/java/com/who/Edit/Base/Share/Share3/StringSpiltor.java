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

	public static class Unicode
	{

		public static CharSequence encode(CharSequence str, String charset) throws UnsupportedEncodingException 
		{
			String zhPattern = "[\u4e00-\u9fa5]+";//正则表达式，用于匹配url里面的中文
			Pattern p = Pattern.compile(zhPattern);
			Matcher m = p.matcher(str);
			StringBuffer b = new StringBuffer();
			while (m.find()) {
				m.appendReplacement(b, URLEncoder.encode(m.group(0), charset));
			}
			m.appendTail(b);
			return b.toString();
		}

		public static byte[] decode(CharSequence str,String charSetName)
		{
			byte[] arr = null;
			try{
				arr = str.toString().getBytes(charSetName);
			}catch (UnsupportedEncodingException e){}
			return arr;
		}

		public static int checkUnicodeCount(String str)
		{
			int count = 0;
			char[] arr = str.toCharArray();
			//如果需要遍历文本每个字符，务必先toCharArray，然后使用下标引用操作
			//因为函数使用次数多了，会额外消耗效率，在50000字吋，它们的差距在4~6ms
			for(int i=arr.length-1;i>=0;--i)
			{
				char c = arr[i];
				if(c>=0x4E00 && c<=0x9FFF){
					//中文字符编码范围
					++count;
				}
			}
			return count;
		}

	}
}
