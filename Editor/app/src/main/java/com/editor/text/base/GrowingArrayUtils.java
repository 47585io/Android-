package com.editor.text.base;


public final class GrowingArrayUtils
{
    /** 
	 * 将元素附加到数组的末尾，如果没有更多空间，则会增加数组。
	 * @param array 追加元素的数组。 这不能为空。
	 * @param currentSize 数组中元素的数量。 必须小于或等于array.length。 
	 * @param element 要追加的元素。
	 * @return 元素附加到的数组。 这可能与给定的数组不同。
	 */
     public static <T> T[] append(T[] array, int currentSize, T element) 
	 {
        assert currentSize <= array.length;
        if (currentSize + 1 > array.length) {
            @SuppressWarnings("unchecked")
				T[] newArray = ArrayUtils.newUnpaddedArray(
				(Class<T>) array.getClass().getComponentType(), growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }
  
    public static int[] append(int[] array, int currentSize, int element)
	{
        assert currentSize <= array.length;
        if (currentSize + 1 > array.length) {
            int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }
   
    public static long[] append(long[] array, int currentSize, long element)
	{
        assert currentSize <= array.length;
        if (currentSize + 1 > array.length) {
            long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }
   
    public static boolean[] append(boolean[] array, int currentSize, boolean element) 
	{
        assert currentSize <= array.length;
        if (currentSize + 1 > array.length) {
            boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }
  
    public static float[] append(float[] array, int currentSize, float element)
	{
        assert currentSize <= array.length;
        if (currentSize + 1 > array.length) {
            float[] newArray = ArrayUtils.newUnpaddedFloatArray(growSize(currentSize));
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize] = element;
        return array;
    }
	
	/** 
	 *在数组中的指定索引处插入一个元素，如果没有更多的空间，则增大数组
	 * @param array要追加元素的数组，不得为空
	 * @param currentSize数组中元素的数量，必须小于或等于array.length
	 * @param element要插入的元素
	 * @返回元素附加到的数组。这可能与给定的数组不同
	 */
    public static <T> T[] insert(T[] array, int currentSize, int index, T element)
	{
        assert currentSize <= array.length;
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        @SuppressWarnings("unchecked")
			T[] newArray = ArrayUtils.newUnpaddedArray((Class<T>)array.getClass().getComponentType(),
													   growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
 
    public static int[] insert(int[] array, int currentSize, int index, int element)
	{
        assert currentSize <= array.length;
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        int[] newArray = ArrayUtils.newUnpaddedIntArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
    
    public static long[] insert(long[] array, int currentSize, int index, long element) 
	{
        assert currentSize <= array.length;
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        long[] newArray = ArrayUtils.newUnpaddedLongArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
   
    public static boolean[] insert(boolean[] array, int currentSize, int index, boolean element) 
	{
        assert currentSize <= array.length;
        if (currentSize + 1 <= array.length) {
            System.arraycopy(array, index, array, index + 1, currentSize - index);
            array[index] = element;
            return array;
        }
        boolean[] newArray = ArrayUtils.newUnpaddedBooleanArray(growSize(currentSize));
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = element;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
	
	/** 
	 * 给定数组的当前大小，返回数组应该增长到的理想大小。
	 * 这通常是给定大小的两倍，但在未来不应依赖。
	 */
    public static int growSize(int currentSize) {
        return currentSize <= 4 ? 8 : currentSize * 2;
	}
	
}
    
