package com.editor.text.base;
import java.lang.reflect.*;

public class ArrayUtils
{
	
	private static final int CACHE_SIZE = 73;
	private static Object[] sCache = new Object[CACHE_SIZE];
	
	private static int MaxObjectSize = 16;
	
	
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
	
	public static int getAlignmentMinimumSize(int size, Class type)
	{
		int min = size%MaxObjectSize;
		if(min==0){
			return size;
		}
		
		min = MaxObjectSize -min;
		if(type==byte.class){
			
		}
		else if(type==char.class){
			min/=2;
		}
		else if(type==int.class || type==float.class){
			min/=4;
		}
		else if(type==long.class){
			min/=8;
		}
		return size+min;
	}
	
	public static <T> T[] emptyArray(Class<T> kind)
	{
        if (kind == Object.class) {
            return (T[]) EmptyArray.OBJECT;
        }
        int bucket = (kind.hashCode() & 0x7FFFFFFF) % CACHE_SIZE;
        Object cache = sCache[bucket];
        if (cache == null || cache.getClass().getComponentType() != kind) {
            cache = Array.newInstance(kind, 0);
            sCache[bucket] = cache;
        }
        return (T[]) cache;
    }
	
}
