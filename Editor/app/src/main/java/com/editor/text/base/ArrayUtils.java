package com.editor.text.base;
import java.lang.reflect.*;
import java.util.*;

public class ArrayUtils
{
	
	private static final int ALIGNMENT_AMOUNT = 8;
	
	/* 创建一个新的指定元素个数的数组，可能比size更大，但这会利用空闲的空间 */
	public static<T> T[] newUnpaddedArray(Class<T> kind, int size){
		return (T[])Array.newInstance(kind,getAlignmentMinimumSize(kind,size));
	}
	
	public static boolean[] newUnpaddedBooleanArray(int size){
		return new boolean[getAlignmentMinimumSize(boolean.class,size)];
	}
	
	public static byte[] newUnpaddedByteArray(int size){
		return new byte[getAlignmentMinimumSize(byte.class,size)];
	}
	
	public static char[] newUnpaddedCharArray(int size){
		return new char[getAlignmentMinimumSize(char.class,size)];
	}
	
	public static int[] newUnpaddedIntArray(int size){
		return new int[getAlignmentMinimumSize(int.class,size)];
	}
	
	public static long[] newUnpaddedLongArray(int size){
		return new long[getAlignmentMinimumSize(long.class,size)];
	}

	public static float[] newUnpaddedFloatArray(int size){
		return new float[getAlignmentMinimumSize(float.class,size)];
	}
	
	public static double[] newUnpaddedDoubleArray(int size){
		return new double[getAlignmentMinimumSize(double.class,size)];
	}
	
	//计算指定元素类型且指定元素个数的数组的内存对齐后的最小元素个数
	public static int getAlignmentMinimumSize(Class kind, int size)
	{
		if(kind==boolean.class || kind==byte.class){
			size = getAlignmentMinimumSize(1,size);
		}
		else if(kind==char.class){
			size = getAlignmentMinimumSize(2,size);
		}
		else if(kind==int.class || kind==float.class){
			size = getAlignmentMinimumSize(4,size);
		}
		else if(kind==long.class || kind==double.class){
			size = getAlignmentMinimumSize(8,size);
		}
		else{
			size = getAlignmentMinimumSize(4,size);
		}
		return size;
	}
	
	//数组要占用的内存长度没有对齐，就将其对齐到下个位置，计算对齐后的元素个数
	private static int getAlignmentMinimumSize(int kind, int size)
	{
		int len = size*kind;
		int over = len%ALIGNMENT_AMOUNT;
		if(over!=0){
			len += ALIGNMENT_AMOUNT-over;
			size = len/kind;
		}
		return size;
	}
	
	/* 在数组中向后寻找指定元素，找到了返回它的下标，从index开始 */
	public static <T> int indexOf(T[] array, T value, int index)
	{
        if (array == null || index<0) return -1;
        for (; index < array.length; index++) {
            if (Objects.equals(array[index], value)) return index;
        }
        return -1;
    }
	/* 在数组中向前寻找指定元素，找到了返回它的下标，从index开始 */
	public static <T> int lastIndexOf(T[] array, T value, int index)
	{
        if (array == null || index>=array.length) return -1;
        for (;index>=0; index--) {
            if (Objects.equals(array[index], value)) return index;
        }
        return -1;
    }
	
	public static int indexOf(char[] array, char value, int index)
	{
        if (array == null || index<0) return -1;
        for (; index < array.length; index++) {
            if (array[index]==value) return index;
        }
        return -1;
    }

	public static int lastIndexOf(char[] array, char value, int index)
	{
        if (array == null || index>=array.length) return -1;
        for (;index>=0; index--) {
            if (array[index]==value) return index;
        }
        return -1;
    }
	
	public static int indexOf(int[] array, int value, int index)
	{
        if (array == null || index<0) return -1;
        for (; index < array.length; index++) {
            if (array[index]==value) return index;
        }
        return -1;
    }

	public static int lastIndexOf(int[] array, int value, int index)
	{
        if (array == null || index>=array.length) return -1;
        for (;index>=0; index--) {
            if (array[index]==value) return index;
        }
        return -1;
    }

	/* 快速排序 */
	public static <T> void quickSort(T[] list, int size, Comparator<T> com)
	{
		if (size > 0 && size < list.length){
			//查看数组是否为空
			//开始分裂排序
			unckSort(list, 0, size - 1, com);
		}
	}
	
	private static <T> void unckSort(T[] list, int low, int high, Comparator<T> com)
	{
		if (low < high){
			int middle = getMiddle(list, low, high, com);    //将list数组一分为二
			unckSort(list, low, middle - 1, com);    // 对左边进行递归排序
			unckSort(list, middle + 1, high, com);    // 对右边进行递归排序
		}
		//继续递归分裂，直至每个小数组只有两个元素
		//则只有low<high，才能继续
		//当一个数组只有两个元素，则此时排序，只是比较两个元素大小
		//因为整个数组都是按序分的
		//所以每个小数组排好序，则大数组也排好了
	}
	
	private static <T> int getMiddle(T[] list, int low, int high, Comparator<T> com)
	{
		T tmp = list[low]; // 数组的第一个值作为中轴（分界点或关键数据）
		while (low < high)
		{
			while (low < high && com.compare(list[high], tmp) >= 0){
				high--;
			}
			//从右边开始找一个小于中点的数
			//如果已经大于中点，则不用挪
			//如果有小于中点的数，挪至左边
			list[low] = list[high]; 
			// 将其移动到list[low],此时list[low]必然小于中点,并且list[low]==list[high]
			while (low < high && com.compare(list[low], tmp) <= 0){
				low++;
			}
			//从list[low]开始找一个大于中点的数，必然不可能是当前list[low]		
			list[high] = list[low]; 
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
			//high说：我右边都比tmp大
			//所以可以把tmp插入这里,这样tmp就移动到中间了 
		}
		list[low] = tmp; // 中点位置
		return low; // 返回中点的位置
	}


	private static int getMiddle(int[] list, int low, int high)
	{
		//数组的第一个值作为中点（分界点或关键数据）
		int tmp = list[low]; 
		while (low < high)
		{
			while (low < high && list[high] >= tmp){
				high--;
			}
			//从右边开始找一个小于中点的数，挪至左边
			//将其移动到list[low],此时list[low]==list[high]
			list[low] = list[high]; 
			
			while (low < high && list[low] <= tmp){
				low++;
			}
			//从左边开始找一个大于中点的数，将这个大于中点的数挪至右边
			//可以挪到list[high]，因为list[high]已经挪到左边了
			list[high] = list[low]; 
			
			//一直交换下去，直至row和high相遇，
			//row说：我左边都比tmp小
			//high说：我右边都比tmp大
		}
		//所以可以把tmp插入这里,这样tmp就移动到中间了
		list[low] = tmp; 
		return low; // 返回中点的位置
	}
	
	/* 二分查找 */
	public static <T> int binarySearch(T[] list, T value, int size, Comparator<T> com)
	{
		if (size <= 0 || size > list.length){
			return -1;
		}
		
		int low = 0;   
		int high = size - 1;   
		while (low <= high)
		{   
			int middle = (low + high) / 2;   
			if (com.compare(list[middle],value)==0) 
				return middle;   
			else if (com.compare(list[middle],value)<0)
				high = middle - 1;   
			else 
				low = middle + 1;
		}  
		return -1;  
	}

}
