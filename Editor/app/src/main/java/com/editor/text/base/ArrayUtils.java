package com.editor.text.base;
import java.lang.reflect.*;

public class ArrayUtils
{
	public static<T> T[] newUnpaddedArray(Class<T> type, int size){
		return (T[])Array.newInstance(type,size);
	}
	
	public static boolean[] newUnpaddedBooleanArray(int size){
		return new boolean[size];
	}
	
	public static byte[] newUnpaddedByteArray(int size){
		return new byte[size];
	}
	
	public static char[] newUnpaddedCharArray(int size){
		return new char[size];
	}
	
	public static int[] newUnpaddedIntArray(int size){
		return new int[size];
	}
	
	public static long[] newUnpaddedLongArray(int size){
		return new long[size];
	}

	public static float[] newUnpaddedFloatArray(int size){
		return new float[size];
	}
	
	public static double[] newUnpaddedDoubleArray(int size){
		return new double[size];
	}
	
}
