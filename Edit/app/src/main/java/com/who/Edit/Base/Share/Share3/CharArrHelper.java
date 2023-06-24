package com.who.Edit.Base.Share.Share3;
import android.text.*;
import android.graphics.*;


/*
________________________________________

 SpannableStringBuilder的增删方法都很快，但速度还是有差距，增删同样字符串，时间消耗:

 append(570) < delete(580) < insert(702) < replace(709)
 
 这里只是大概，insert在尾部插入时效率更高
 
________________________________________
 
 至于获取字符串的方法呢，SpannableStringBuilder有三个选择:

 toString
 subSequence
 getChars
 
 下面我们测试一下它们的效率如何
 
________________________________________

 我们先考虑插入效率:
 SpannableStringBuilder只能插入CharSequence及其子类，当已有一个CharSequence，要插入它

 如果要全部插入，您可以直接提交这个CharSequence，效率最高，但如果它不包含Span，效率更高
 
 如果只要插入其中一个范围内的字符串
 SpannableStringBuilder支持只插入指定字符串的一个范围内的字符，即insert(int start, CharSequence tb, int tbStart, int tbEnd)，效率最高，
 (因为SpannableStringBuilder的这个方法会只复制指定范围内的字符)

 也可以先从原字符串中subSequence一串再插入，效率较低
 (说实话，无论如何，SpannableStringBuilder的插入方法都是直接复制字符的，所以没必要截取了再插)
 
 也可以先getChars，再用String.valueOf转化为字符串后插入，效率很低，并且getChars是GetChars接口的方法，不是CharSequence的方法
 (说实话，无论如何都不推荐使用这个方法)

________________________________________

 我们再考虑获取效率:
 我现在需要获取一串String或字符数组以便快速查找，因为SpannableStringBuilder的charAt调用次数多了太慢

 从SpannableStringBuilder中获取全部的文本，可以直接toString，效率较低
 (其实，toString方法的效率，等于先getChars全部字符的数组，再String.valueOf将数组转化为字符串)

 如果要获取一个范围内的文本，可以先subSequence，再toString，效率较低，因为截取出来的也是一个SpannableStringBuilder，有着一样慢的toString方法
 (说实话，与其builder.subSequence(start,end).toString()，不如String.valueOf(builder.getChars(start,end,arr,0))

 但有一个更快的方法，您可以直接getChars，这样它会将指定范围内的字符拷贝到指定字符数组中，并且数组可循环使用，并且数组遍历效率高，代价是您需要自己在字符数组中查找
 (可以借助CharArrHelper类帮助您在字符数组中查找字符)

________________________________________

 总结:

 插入时能直接插就直接插，如果只插一个范围内的字符串，直接使用insert(int start, CharSequence tb, int tbStart, int tbEnd)效率最高

 至于获取，如果获取的字符较少，可以循环charAt，如果获取的字符较多，无论怎样getChars效率最高

________________________________________

*/
public class CharArrHelper
{
	
	public static int Count(char c,char[] arr,int start,int end)
	{
		int count = 0;
	    for(;start<end;++start)
		{
			if(arr[start]==c){
				++count;
			}
		}
		return count;
	}
	
	public static int NIndex(char c,char[] arr,int index,int n)
	{
		for(;index<arr.length;++index)
		{
			if(arr[index]==c){
				--n;
			}
			if(n<1){
				break;
			}
		}
		return index;
	}
	
	public static int lastNIndex(char c,char[] arr,int index,int n)
	{
		for(;index>-1;--index)
		{
			if(arr[index]==c){
				--n;
			}
			if(n<1){
				break;
			}
		}
		return index;
	}
	
	public static int indexOf(char c, char[] arr, int index)
	{
		for(;index<arr.length;++index)
		{
			if(arr[index]==c){
				break;
			}
		}
		return index;
	}
	
	public static int lastIndexOf(char c, char[] arr, int index)
	{
		for(;index>-1;--index)
		{
			if(arr[index]==c){
				break;
			}
		}
		return index;
	}
	
}
