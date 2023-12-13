package com.editor.text2.base.share3;
import com.editor.text2.base.share4.*;
import com.editor.text2.base.share1.*;
import android.text.*;
import java.util.*;
import com.editor.text.base.*;

public class StringChecker
{
	//字符是否为A~z
	public static boolean IsAtoz(char ch)
	{
		if(ch>='a' && ch <='z')
			return true;
		else if(ch>='A' && ch <='Z')
			return true;
		return false;
	}
	//字符是否为数字
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
			char ch = src.charAt(i);
			if(ch<'0'||ch>'9')
				return false;
		}
		return true;
	}
	
	public static class Bindow
	{
		public static range checkIndexInBindowRange(char[] text, int index, char stBindow, char enBindow)
		{
			int queryStart = 0, queryEnd = text.length;
			//括号的范围包含括号内的内容，括号本身，以及紧挨括号的位置
			if(index > queryStart && index <= queryEnd && text[index-1] == enBindow){
				index--;
			}
			int start = ArrayUtils.lastIndexOf(text,stBindow,index,queryStart);
			if(start < 0){
				return null;
			}
			int end = ArrayUtils.indexOf(text,enBindow,index,queryEnd);
			if(end < 0){
				return null;
			}
			
			//临近的后括号不应该是end，因为这样我会多找一次
			int nearStart = end == index ? ArrayUtils.lastIndexOf(text,enBindow,index-1,queryStart) : ArrayUtils.lastIndexOf(text,enBindow,index,queryEnd);	
			if(nearStart > -1 && nearStart > start)
			{
				//我前面是后括号，意味着我所在的括号范围的前括号在更前面
				//从当前后括号开始，向前配对括号，直至找到孤单的前括号
				int count = 1;
				nearStart--;
				while(true)
				{
					int bindowStart = ArrayUtils.lastIndexOf(text,stBindow,nearStart,queryStart);	
					int bindowEnd = ArrayUtils.lastIndexOf(text,enBindow,nearStart,queryStart);
					if(bindowStart < 0){
						//没有前括号了，可能文本中的括号不能完美配对
						return null;
					}
					if(bindowEnd < 0 || bindowStart > bindowEnd){
						//临近的括号是前括号，就与一个后括号配对，如果没有后括号与之配对，它就是孤单的前括号
						if(count == 0){
							start = bindowStart;
							break;
						}
						count--;
						nearStart = bindowStart-1;
					}
					else{
						//临近的括号是后括号，就存入一个后括号等待前括号与之配对
						count++;
						nearStart = bindowEnd-1;
					}
				}
			}
			int nearEnd = start == index ? ArrayUtils.indexOf(text,stBindow,index+1,queryEnd) : ArrayUtils.indexOf(text,stBindow,index,queryEnd);
			if(nearEnd > -1 && nearEnd < end)
			{
				//我后面是前括号，意味着我所在的括号范围的后括号在更后面
				//从当前前括号开始，向后配对括号，直至找到孤单的后括号
				int count = 1;
				nearEnd++;
				while(true)
				{
					int bindowStart = ArrayUtils.indexOf(text,stBindow,nearEnd,queryEnd);	
					int bindowEnd = ArrayUtils.indexOf(text,enBindow,nearEnd,queryEnd);
					if(bindowEnd < 0){
						return null;
					}
					if(bindowStart < 0 || bindowEnd < bindowStart)
					{
						if(count == 0){
							end = bindowEnd;
							break;
						}
						count--;
						nearEnd = bindowEnd+1;
					}
					else{
						count++;
						nearEnd = bindowStart+1;
					}
				}
			}
			return new range(start,end);
		}
		public static range checkIndexInBindowRange2(CharSequence text, int index, char stBindow, char enBindow)
		{
			//括号的范围包含括号内的内容，括号本身，以及紧挨括号的位置
			if(index > 0 && index <= text.length() && text.charAt(index-1) == enBindow){
				index--;
			}
			int nearStart = text.charAt(index) == enBindow ? findNearCharBefore(text,index-1,stBindow,enBindow) : findNearCharBefore(text,index,stBindow,enBindow);
			if(nearStart < 0){
				return null;
			}
			if(text.charAt(nearStart) != stBindow)
			{
				int count = 1;
				nearStart--;
				while(true)
				{
					nearStart = findNearCharBefore(text,nearStart,stBindow,enBindow);
					if(nearStart < 0){
						return null;
					}
					if(text.charAt(nearStart) == stBindow)
					{
						if(count == 0){
							break;
						}
						count--;
					}
					else{
						count++;
					}
					nearStart--;
				}
			}
			int nearEnd = text.charAt(index) == stBindow ? findNearCharAfter(text,index+1,stBindow,enBindow) : findNearCharAfter(text,index,stBindow,enBindow);
			if(nearEnd < 0){
				return null;
			}
			if(text.charAt(nearEnd) != enBindow)
			{
				int count = 1;
				nearEnd++;
				while(true)
				{
					nearEnd = findNearCharAfter(text,nearEnd,stBindow,enBindow);
					if(nearEnd < 0){
						return null;
					}
					if(text.charAt(nearEnd) == enBindow)
					{
						if(count == 0){
							break;
						}
						count--;
					}
					else{
						count++;
					}
					nearEnd++;
				}
			}
			return new range(nearStart,nearEnd);
		}
		private static int findNearCharBefore(CharSequence text, int index, char c1, char c2)
		{
			if(index<0 || index>=text.length()){
				return -1;
			}
			for(;index>-1;--index){
				char ch = text.charAt(index);
				if(ch == c1 || ch == c2){
					return index;
				}
			}
			return -1;
		}
		private static int findNearCharAfter(CharSequence text, int index, char c1, char c2)
		{
			int length = text.length();
			if(index<0 || index>=length){
				return -1;
			}
			for(;index<length;++index){
				char ch = text.charAt(index);
				if(ch == c1 || ch == c2){
					return index;
				}
			}
			return -1;
		}
	}
	
	public static int KMP(char[] ts, char[] ps, int[] psNext, int tsStart)
	{
		char[] t = ts;
		char[] p = ps;
		int lent = ts.length;
		int lenp = ps.length;
		int i = tsStart; //父串的位置  
		int j = 0; //子串的位置
		int[] next = psNext;
		
		//当父串未比完并且子串未比完
		while(i < lent && j < lenp)
		{
			if(j == -1 || t[i] == p[j])
			{
				//当j为-1时，要移动的是i，当然j也要归0 
				//或者从起始位置开始，之后的字符相同，则都往后继续比较
				i++;
				j++;
			}
			else{
				//当匹配到不相同了，i不需要回溯了，j回到指定位置
				j = next[j];
			}
		}
		if (j == lenp){
			//子串走完，则返回子串在在父串中的起始位置，即当前i-j，相当于从i开始，向前走一个子串的长度，则本次子串首字符下标
			//将子串判断条件放前面是有道理的，如果在某字符串中，子串比较到父串中最后一个字符才比较成功，则此时父串也走完了，子串也走完了，所以必须先判断子串是否走完
			return i - j;
		}
		else{
			//如果父串走完并且子串未走完，则返回-1
			return -1;
		}
	}
	public static int[] getNext(char[] ps)
	{
		char[] p = ps; 
		int length = ps.length;
		int next[] = new int[length];
		//存储当j为j时匹配失败时需要调整的下标值k，需要调整的下标的个数绝对小于或等于子串长度，因为匹配失败时都是子串没比较完
		
		next[0] = -1; //当j=0时匹配失败，则需要调整的下标k总为-1
		int j = 0;    //记录子串中当前字符、下标j
		int k = -1;   //为j时，需要调整的下标k

		//length是子串的字符个数，再减1是子串的最后一个元素下标，当小于最后一个元素时，将持续往next数组中下标为j的元素存入k(就是把j匹配失败时下标j所有可能出现的情况都存了)
		while(j < length-1)
		{
			if(k == -1 || p[j] == p[k])
			{
				//如果k为-1，说明现在是子串下标为0(j=0)时，则去调整一下，子串下标为1(++j)时匹配失败调整的下标，则为0(++k)
				//或者，也就是之后(除0或1)，匹配失败，并且最大重复串之后的一个字符相同，则j+1为上次最大重复串的延伸
				//或者，由k=next[k]而来，则尽量保证的是最大重复串，并且最后一个字符p[k]与p[j]，则j+1的后缀串k2为前缀串加上那个字符后的字符串
				//或者此时的-1是因为已经没有p[j]之前的字符串，并且p[j]!=p[k]，则k=next[0]，则j+1之前完全没有后缀串，此时j+1时的后缀串为0
				if(p[++j] == p[++k])
				{
					//注意先++，再使用，必须保证j与之前不变的情况下才继续下面的操作
					//如果j+1时匹配失败，则调整的字符下标为++k，如果j+1时匹配失败的字符与要调整的下标字符相同
					//缩进字符串，使得后缀串之后的字符尽量不为p[j]，相当于之前的p[k]跳到后面
					//注意: 我们只是将next[j+1]重新赋了一个更合适的next[k]，而并没有修改此时k的值，即k的值仍是j+1之前的最大前缀串，之后求j+2仍可使用k
					//因为如果修改了，那根本就失去了当前在此处的j时之前能拥有的最大的后缀串，这对我们之后求j+1是至关重要的(由此延伸或尽量保留其中最大后缀串(代表着j之前只有这一串最大的与子串前半部分匹配的串))
					next[j] = next[k];
				}
				else{
					//如果j+1时匹配失败的字符与要调整的下标字符不相同，这是不是j+1时，要赋值的++k
					next[j] = k;
				}
			}
			else{
				//缩进至当前p[k]的前面的字符串的最大重复串k1
				k = next[k];
			}
		}
		return next; 
	}
	
}
