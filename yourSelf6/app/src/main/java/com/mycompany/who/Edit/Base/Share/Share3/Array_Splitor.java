package com.mycompany.who.Edit.Base.Share.Share3;

import java.util.*;

public class Array_Splitor
{
	
	public static final Comparator<CharSequence> sortStrForChar = new sortStrForChar();
	
	public static final Comparator<CharSequence> sortStrForLen = new sortStrForLen();
	

	public static<T> void toArray(Collection<T> coll, T[] arr)
	{
		if (coll != null)
		    coll.toArray(arr);
	}
	public static<T> ArrayList<T> toList(T[] arr)
	{
		ArrayList<T> coll = new ArrayList<>();
		if (arr != null)
		    for (T s:arr)
		        coll.add(s);
		return coll;
	} 

	public static int getmin(int start, int end, int ... arr)
	{
		int min=Integer.MAX_VALUE;
		for (int tmp:arr)
		{
			if (tmp < min && tmp >= start && tmp <= end)
			{
				min = tmp;
			}
		}
		if (min == Integer.MAX_VALUE)
			return -1;
		return min;
	}
	public static int getmax(int start, int end, int ... arr)
	{
		int max=Integer.MIN_VALUE;
		for (int tmp:arr)
		{
			if (tmp > max && tmp >= start && tmp <= end)
			{
				max = tmp;
			}
		}
		if (max == Integer.MIN_VALUE)
			return -1;
		return max;
	}

	public static int indexOf(char ch, char[]fuhao)
	{
		//字符是否在排好序的数组
		if (fuhao == null)
			return -1;
		int low = 0;   
		int high = fuhao.length - 1;   
		while (low <= high)
		{   
			int middle = (low + high) / 2;   
			if (ch == fuhao[middle]) 
				return middle;   
			else if (ch < fuhao[middle])
				high = middle - 1;   
			else 
				low = middle + 1;
		}  
		return -1;  
	}

	public static int indexOf(CharSequence str, CharSequence[] keyword)
	{	
		//字符串是否在排好序的数组
	    if (str.length() == 0 || keyword == null)
			return -1;
		int start=0;
		for (;start < keyword.length;start++)
			if (keyword[start].charAt(0) == str.charAt(0))
			{			
				break;
			}
		for (;start < keyword.length && str.charAt(0) == keyword[start].charAt(0) ;start++)
			if (keyword[start].equals(str))
				return start;
		return -1;
	}

	public static List<CharSequence> indexsOf(CharSequence str, CharSequence[] keyword, int start, Idea i)
	{	
		//查找数组中所有出现了str的元素
		if (str.length() == 0 || keyword == null || keyword.length == 0)
			return null;
	    List<CharSequence> words = new ArrayList<>();
		for (CharSequence word:keyword)
		{
			if (i.can(word, str, start))
			{
				words.add(word);
			}
		}
		return words;
	}

	public static void sortStrForChar(List<CharSequence> words)
	{
		quickSort(words,sortStrForChar);
	}

	public static void sortStrForLen(List<CharSequence> words)
	{
		quickSort(words,sortStrForLen);
	}


	protected static<T> int getMiddle(List<T> list, int low, int high, Comparator<T> com)
	{
		T tmp = list.get(low); // 数组的第一个值作为中轴（分界点或关键数据）
		while (low < high)
		{
			while (low < high && com.compare(list.get(high), tmp) >= 0)
			{
				high--;
			}
			//从右边开始找一个小于中点的数
			//如果已经大于中点，则不用挪
			//如果有小于中点的数，挪至左边
			list.set(low, list.get(high)); 
			// 将其移动到list[low],此时list[low]必然小于中点,并且list[low]==list[high]
			while (low < high && com.compare(list.get(low), tmp) <= 0)
			{
				low++;
			}
			//从list[low]开始找一个大于中点的数，必然不可能是当前list[low]		
			list.set(high, list.get(low)); 
			//将这个大于中点的list[low]挪至右边
			//可以挪到list[high]，因为list[high]已经挪到左边了

			//那原来的list[low]的值不要了吗？list[high]挪到的那个

			//别急，让我们看下次循环，
			//当在右边找到一个，是不是又移动到左边，那么此时移动到？
			//代码显示是当前的low
			//那么这个low其实上次就已移动到high了，所以新的high可以移动到low，并且是安全的
			//之后又一个low，移动到上次的high，high刚移左边
			//这一步步衔接的太妙了吧
			//不难发现，当最后一次循环，row移动到high，那么此时row并无用，完全可以把tmp（中点）插入这里
			//那么row和high相遇，
			//row说：我左边都比tmp小
			//high：我右边都比tmp大
			//所以可以把tmp插入这里,这样tmp就移动到中间了 
		}
		list.set(low, tmp); // 中点位置
		return low; // 返回中点的位置
	}

	protected static<T> void unckSort(List<T> list, int low, int high, Comparator<T> com)
	{
		if (low < high)
		{
			int middle = getMiddle(list, low, high, com);    // 将list数组一分为二
			unckSort(list, low, middle - 1, com);    // 对左边进行递归排序
			unckSort(list, middle + 1, high, com);    // 对右边进行递归排序
		}
		//继续递归分裂，直至每个小数组只有两个元素
		//则只有low<high，才能继续
		//当一个数组只有两个元素，则此时排序，只是比较两个元素大小
		//因为整个数组都是按序分的
		//所以每个小数组排好序，则大数组也排好了
	}

	/* 推荐使用ArrayList，它是最快的，因为我们不需要add或remove，只要get和set */
	public static<T> void quickSort(List<T> str, Comparator<T> com)
	{
		if (str.size() > 0)
		{
			// 查看数组是否为空
			//开始分裂排序
			unckSort(str, 0, str.size() - 1, com);
		}
	}


	public static class sortStrForChar implements Comparator<CharSequence>
	{
		@Override
	    public int compare(CharSequence p1, CharSequence p2)
	    {
		    if (p1.charAt(0) < p2.charAt(0))
			{
			    return 1;
		    }
		    else if (p1.charAt(0) == p2.charAt(0))
			    return 0;
		    else
			    return -1;
	    }
	}

	public static class sortStrForLen implements Comparator<CharSequence>
	{
	    @Override
		public int compare(CharSequence p1, CharSequence p2)
		{
			if (p1.length() > p2.length())
				return 1;
			else
				return -1;

		}
	};

}

