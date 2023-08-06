package com.editor.text.base;
import java.lang.reflect.*;
import java.util.*;

public class ArrayUtils
{
	private static final int ALIGNMENT_AMOUNT = 8;
	
	public static<T> T[] newUnpaddedArray(Class<T> type, int size){
		return (T[])Array.newInstance(type,getAlignmentMinimumSize(type,size));
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
	
	public static <T> int indexOf(T[] array, T value, int index)
	{
        if (array == null) return -1;
        for (; index < array.length; index++) {
            if (Objects.equals(array[index], value)) return index;
        }
        return -1;
    }
	
	public static <T> int lastIndexOf(T[] array, T value, int index)
	{
        if (array == null || index>=array.length) return -1;
        for (;index>=0; index--) {
            if (Objects.equals(array[index], value)) return index;
        }
        return -1;
    }
	
	public static <T> int Count(T[] array, T value, int start, int end)
	{
		int count = 0;
		for(;start<end;++start)
		{
			if(Objects.equals(array[start],value)) ++count;
		}
		return count;
	}
	
	public static int indexOf(char[] array, char value, int index)
	{
        if (array == null) return -1;
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

	public static int Count(char[] array, char value, int start, int end)
	{
		int count = 0;
		for(;start<end;++start)
		{
			if(array[start]==value) ++count;
		}
		return count;
	}

}
