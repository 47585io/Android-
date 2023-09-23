package com.editor.text2.base.share3;
import com.editor.text2.base.share4.*;

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
			if(!IsNumber(src.charAt(i)))
				return false;
		}
		return true;
	}
	
}
