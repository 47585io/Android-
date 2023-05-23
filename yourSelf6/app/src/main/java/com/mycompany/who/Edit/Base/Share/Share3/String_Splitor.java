package com.mycompany.who.Edit.Base.Share.Share3;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;


public class String_Splitor
{
	
	public static boolean IsAtoz(char ch)
	{
		if(ch>='a' && ch <='z')
			return true;
		else if(ch>='A' && ch <='Z')
			return true;
		else if(ch=='_')
			return true;
			//无奈
		return false;
	}

	public static boolean IsNumber(char ch)
	{
		if(ch>='0'&&ch<='9')
			return true;
		return false;
	}
	public static boolean IsNumber(CharSequence src)
	{
		int i,len=src.length();
	    for(i=0;i<len;++i){
			if(!IsNumber(src.charAt(i)))
				return false;
		}
		return true;
	}
	
	public static CharSequence indexOfKey(String str,int nowIndex,Map<CharSequence,CharSequence> zhu_key_value)
	{
		for(CharSequence key: zhu_key_value.keySet()){
		    if(str.indexOf(key.toString(),nowIndex)==nowIndex)
				return key;
		}
		return null;
	}
	
	public static List<Integer> indexsOf(String str,String text)
	{
		//查找文本中所有出现str的index
		int index = 0,len = str.length();
		List<Integer> indexs = new ArrayList<Integer>();
		while(true){
		    index = text.indexOf(str,index);
			if(index==-1)
				break;
			indexs.add(index);
			index+=len;
		}
		return indexs;
	}
	public static List<Integer> indexsOf(char c,String text)
	{
		//查找文本中所有出现str的index
		int index = 0;
		List<Integer> indexs = new ArrayList<Integer>();
		while(true){
		    index = text.indexOf(c,index);
			if(index==-1)
				break;
			indexs.add(index);
			++index;
		}
		return indexs;
	}
	
	public static int Count(String want,String text){
		int count=0;
		int index =0;
		while(true){
		    index = text.indexOf(want,index);
			if(index==-1)
				break;
			++count;
			index+=want.length();
		}
		return count;
	}
	public static int Count(char want,String text){
		int count=0;
		int index =0;
		while(true){
		    index = text.indexOf(want,index);
			if(index==-1)
				break;
			++count;
			++index;
		}
		return count;
	}
	
	//字符c第n次出现的下标
	public static int NIndex(char c,String text,int n)
	{
		int index = 0;
		while(n-->0){
		    int tmp = text.indexOf(c,index);
			if(tmp==-1)
				break;
			index = tmp;
			++index;
		}
		return index;
	}
	public static int NIndex(String c,String text,int n)
	{
		int index = 0;
		while(n-->0){
		    int tmp = text.indexOf(c,index);
			if(tmp==-1)
				break;
			index = tmp;
			index+=c.length();
		}
		return index;
	}
	
	public static int calaN(CharSequence src,int index)
	{
		int count = 0;
		while(index<src.length()&&(src.charAt(index)==' '||src.charAt(index)=='\t')){
			count++;
			index++;
		}
		return count;
	}
	public static CharSequence getNStr(CharSequence src,int n)
	{
		StringBuilder arr= new StringBuilder("");
		while(n-- >0){
			arr.append(src);
		}
		return arr.toString();
	}
	
	/*
	 GB2312-80 是 1980 年制定的中国汉字编码国家标准。共收录 7445 个字符，其中汉字 6763 个。
	 GB2312 兼容标准 ASCII码，采用扩展 ASCII 码的编码空间进行编码，一个汉字占用两个字节，每个字节的最高位为 1。
	 具体办法是：收集了 7445 个字符组成 94*94 的方阵，每一行称为一个“区”，每一列称为一个“位”，区号位号的范围均为 01-94，区号和位号组成的代码称为“区位码”，我们分别用两个字节表示它们，范围为0x0101~0x5E5E
	 区位输入法就是通过输入区位码实现汉字输入的。将区号和位号分别加上0x20，得到的 4 位十六进制整数称为国标码，编码范围为 0x2121～0x7E7E。
	 为了兼容标准 ASCII 码，给国标码的每个字节加0x80，形成的编码称为机内码，简称内码，是汉字在机器中实际的存储代码GB2312-80 标准的内码范围是 0xA1A1～0xFEFE
	*/
	public static class Unicode
	{
		
	}
	
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
	
	
	public static class Bindow
	{
		
		public static int getBeforeBindow(String text,int endIndex,String st,String en)
		{
			//用栈把前括号一个个收起来，每遇到一个后括号pop一个，直至遇到指定的后括号，就返回对应前括号
			int now=0;
			Stack<Integer> stack=new Stack<>();
			while(now<text.length())
			{
				int start = text.indexOf(st,now);
				int end = text.indexOf(en,now);

				now = start<end ? start:end;
				//继续向后找前括号或后括号中更小的
				now = now==-1 ? start:now; 
				//now不等-1，则还是now，否则先假设start不为-1
				now = now==-1 ? end:now;
				//now不等-1，则还是now，否则now一定为start，则假设end不为-1

				if(now == -1){
					//如果它们都是-1，则返回
					return -1;
				}
				else if(now==endIndex){
					//遇到指定的后括号，返回对应前括号
					start = stack.size()>0 ? stack.pop():-1;
					return start;
				}
				else if(end==now){
					//最近的前括号已经对应一个最近的后括号，pop
					if(stack.size()>0){
						stack.pop();
					}
					now+=en.length();
				}
				else if(start==now){
					//遇到前括号，push这个前括号
					stack.push(start);
					now+=st.length();
				}

			}
			return -1;
		}

		public static int getAfterBindow(String text,int stratIndex,String st,String en)
		{
			//用栈把后括号一个个收起来，每遇到一个前括号pop一个，直至遇到指定的前括号，返回对应后括号
			int now=text.length();
			Stack<Integer> stack=new Stack<>();
			while(now>-1)
			{
				int start = text.lastIndexOf(st,now);
				int end = text.lastIndexOf(en,now);
				now = start<end ? end:start;
				//继续向前找前括号或后括号最更大的
				now = now==-1 ? start:now; 
				//now不等-1，则还是now，否则先假设start不为-1
				now = now==-1 ? end:now;
				//now不等-1，则还是now，否则now一定为start，则假设end不为-1

				if(now==-1){
					//如果它们都是-1，则返回
					return -1;
				}
				if(now==stratIndex){
					//遇到指定的前括号，pop对应后括号
					end = stack.size()>0 ? stack.pop():-1;
					return end;
				}
				else if(start==now){
					//最近的前括号已经对应一个最近的后括号，pop后括号
					if(stack.size()>0){
						stack.pop();
					}
					now-=st.length();
				}
				else if(end==now){
					//遇到后括号，push
					stack.push(start);
					now-=en.length();
				}
			}
			return -1;
		}


		public static void checkBindow(String text,String st,String en,List<size> stWithEn)
		{
			int now = 0;
			Stack<Integer> stack=new Stack<>();
			while(true)
			{
				int start = text.indexOf(st,now);
				int end = text.indexOf(en,now);
				now = start<end ? start:end;
				//继续向后找前括号或后括号中更小的
				now = now==-1 ? start:now; 
				//now不等-1，则还是now，否则先假设start不为-1
				now = now==-1 ? end:now;
				//now不等-1，则还是now，否则now一定为start，则假设end不为-1

				if(now == -1){
					//如果它们都是-1，则返回
					return;
				}
				else if(end==now){
					//最近的前括号已经对应一个最近的后括号，存入这对括号
					//如果这个括号没有对应括号，则为-1
					start = stack.size()>0 ? stack.pop():-1;
					stWithEn.add(new size(start,end));
					now+=en.length();
				}
				else if(start==now){
					//遇到前括号，push这个前括号
					stack.push(start);
					now+=st.length();
				}
			}
		}

		public static void checkBindow(String text,String st,String en,HashMap<Integer,Integer> stToEn)
		{
			int now = 0;
			Stack<Integer> stack=new Stack<>();
			while(true)
			{
				int start = text.indexOf(st,now);
				int end = text.indexOf(en,now);
				now = start<end ? start:end;
				//继续向后找前括号或后括号中更小的
				now = now==-1 ? start:now; 
				//now不等-1，则还是now，否则先假设start不为-1
				now = now==-1 ? end:now;
				//now不等-1，则还是now，否则now一定为start，则假设end不为-1

				if(now == -1){
					//如果它们都是-1，则返回
					return;
				}
				else if(end==now){
					//最近的前括号已经对应一个最近的后括号，存入这对括号
					//如果这个括号没有对应括号，则为-1
					start = stack.size()>0 ? stack.pop():-1;
					stToEn.put(start,end);
					now+=en.length();
				}
				else if(start==now){
					//遇到前括号，push这个前括号
					stack.push(start);
					now+=st.length();
				}
			}
		}

		/*
		 使用checkBindow返回的一定是从内层到外层括号的正序范围
		 如果index在几层括号重叠内，只要正序向后就可从内到外遍历
		 如果index不在之前的括号块内，也不影响之后的括号块遍历
		 */
		public static size indexInBindowRange(int index,List<size> indexs)
		{
			for(size i:indexs)
			{
				if(i.start<index && i.end>=index){
					return i;
				}
			}
			return null;
		}
		
	}
	
}
