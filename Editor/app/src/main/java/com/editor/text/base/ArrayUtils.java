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
	
	/* 创建一个新数组并拷贝原数组中的内容 */
	public static<T> T[] copyNewArray(T[] array, int oldSize, int newSize)
	{
		T[] newArray = newUnpaddedArray((Class<T>)array.getClass().getComponentType(),newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
	}
	public static boolean[] copyNewBooleanArray(boolean[] array, int oldSize, int newSize)
	{
		boolean[] newArray = newUnpaddedBooleanArray(newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
	}
	public static char[] copyNewCharArray(char[] array, int oldSize, int newSize)
	{
		char[] newArray = newUnpaddedCharArray(newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
	}
	public static int[] copyNewIntArray(int[] array, int oldSize, int newSize)
	{
		int[] newArray = newUnpaddedIntArray(newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
	}
	public static long[] copyNewLongArray(long[] array, int oldSize, int newSize)
	{
		long[] newArray = newUnpaddedLongArray(newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
	}
	public static float[] copyNewFloatArray(float[] array, int oldSize, int newSize)
	{
		float[] newArray = newUnpaddedFloatArray(newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
	}
	public static double[] copyNewDoubleArray(double[] array, int oldSize, int newSize)
	{
		double[] newArray = newUnpaddedDoubleArray(newSize);
		System.arraycopy(array,0,newArray,0,oldSize);
		return newArray;
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
	
	public static int indexOf(char[] array, char value, int index, int end)
	{
        if (array == null || index<0) return -1;
        for (; index < end; index++) {
            if (array[index]==value) return index;
        }
        return -1;
    }
	public static int lastIndexOf(char[] array, char value, int index, int begin)
	{
        if (array == null || index>=array.length) return -1;
        for (;index >= begin; index--) {
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

	public static <T> void quickSort(int[] list, int size)
	{
		if (size > 0 && size < list.length){
			//查看数组是否为空
			//开始分裂排序
			unckSort(list, 0, size - 1);
		}
	}
	private static void unckSort(int[] list, int low, int high)
	{
		if (low < high){
			int middle = getMiddle(list, low, high);    //将list数组一分为二
			unckSort(list, low, middle - 1);    // 对左边进行递归排序
			unckSort(list, middle + 1, high);    // 对右边进行递归排序
		}
		//继续递归分裂，直至每个小数组只有两个元素
		//则只有low<high，才能继续
		//当一个数组只有两个元素，则此时排序，只是比较两个元素大小
		//因为整个数组都是按序分的
		//所以每个小数组排好序，则大数组也排好了
	}
	private static int getMiddle(int[] list, int low, int high)
	{
		//数组的第一个值作为中点（分界点或关键数据）
		int tmp = list[low]; 
		while (low < high)
		{
			//从右边开始找一个小于中点的数，挪至左边
			//将其移动到list[low],此时list[low]==list[high]
			while (low < high && list[high] >= tmp){
				high--;
			}		
			list[low] = list[high]; 
			
			//从左边开始找一个大于中点的数，将这个大于中点的数挪至右边
			//可以挪到list[high]，因为list[high]已经挪到左边了
			while (low < high && list[low] <= tmp){
				low++;
			}
			list[high] = list[low]; 
			
			//一直交换下去，直至row和high相遇，
			//row说：我左边都比tmp小
			//high说：我右边都比tmp大
		}
		//所以可以把tmp插入这里,这样tmp就移动到中间了
		list[low] = tmp; 
		return low; // 返回中点的位置
	}
	
}
