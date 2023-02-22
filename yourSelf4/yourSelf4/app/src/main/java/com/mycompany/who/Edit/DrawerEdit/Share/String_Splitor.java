package com.mycompany.who.Edit.DrawerEdit.Share;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import com.mycompany.who.Edit.DrawerEdit.*;

public class String_Splitor
{
	
	public static boolean IsAtoz(char ch){
		if(ch>='a' && ch <='z')
			return true;
		else if(ch>='A' && ch <='Z')
			return true;
		else if(ch=='_')
			return true;
			//无奈
		return false;
	}

	public static boolean indexOfNumber(char ch){
		if(ch>='0'&&ch<='9')
			return true;
		return false;
	}

	public static boolean indexOfNumber(String src){
		int i;
	    for(i=0;i<src.length();i++){
			if(!indexOfNumber(src.charAt(i)))
				return false;
		}
		return true;
	}
	public static String indexOfKey(String str,int nowIndex,HashMap<String,String> zhu_key_value){
		for(String key: zhu_key_value.keySet()){
		    if(str.indexOf(key,nowIndex)==nowIndex)
				return key;
		}
		return null;
	}
	public static ArrayList<Integer> indexsOf(String str,String text){
		//查找文本中所有出现str的index
		if(str.length()==0||text.length()==0)
			return null;
		int index = 0-str.length();
		ArrayList<Integer> indexs = new ArrayList<Integer>();
		while(true){
		    index = text.indexOf(str,index+str.length());
			if(index==-1)
				break;
			indexs.add(index);
		}
		return indexs;
	}
	
	public static int calaN(String src,int index){
		int count = 0;
		while(index<src.length()&&(src.charAt(index)==' '||src.charAt(index)=='\t')){
			if(src.charAt(index)=='\t'){
				count+=4;
			}
			count++;
			index++;
		}
		return count;
	}
	public static String getNStr(String src,int n){
		StringBuffer arr= new StringBuffer();
		while(n-- !=0){
			arr.append(src);
		}
		return arr.toString();
	}
	
	public static boolean isnullStr(String str){
		//字符串是否为null，是否为""，是否为全空格
		if(str==null)
			return true;
		if(str.length()==0)
			return true;
		int i;
		for(i=0;i<str.length();i++){
		    if(str.charAt(i)!=' ')
				return false;
		}
		return true;
	}
	
	
	public static String encode(String str, String charset) throws UnsupportedEncodingException {
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
	
	public static int getBeforeBindow(String text,int index,String st,String en){
		int now=0;
		Stack<Integer> stack=new Stack<>();
		while(now!=-1){
			now++;
			int start = text.indexOf(st,now);
			int end = text.indexOf(en,now);
			now=Array_Splitor.getmin(0,index,start,end);
			if(now==index-en.length() &&stack.size()>0){
				return stack.pop();
			}
			else if(end==now&&stack.size()>0){
				stack.pop();
			}
			else if(start==now){
				stack.push(start);
			}
			
		}
		return -1;
	}
	
	
	
}
