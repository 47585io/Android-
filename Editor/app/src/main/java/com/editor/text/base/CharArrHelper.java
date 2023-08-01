package com.editor.text.base;

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
