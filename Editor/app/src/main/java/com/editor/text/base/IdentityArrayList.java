package com.editor.text.base;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;


public class IdentityArrayList<E> extends AbstractList<E> 
{
	
    int size;
    transient Object[] array;
	private static final int MIN_CAPACITY_INCREMENT = 12;
 
	public IdentityArrayList() {
        array = EmptyArray.OBJECT;
    }
    public IdentityArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        array = (capacity == 0 ? EmptyArray.OBJECT : new Object[capacity]);
    }
    public IdentityArrayList(Collection<? extends E> collection) 
	{
        Object[] a = collection.toArray();
        if (a.getClass() != Object[].class) {
            Object[] newArray = new Object[a.length];
            System.arraycopy(a, 0, newArray, 0, a.length);
            a = newArray;
        }
        array = a;
        size = a.length;
    }
 
    @Override public boolean add(E object)
	{
        Object[] a = array;
        int s = size;
        if (s == a.length) {
            Object[] newArray = new Object[s +
				(s < (MIN_CAPACITY_INCREMENT / 2) ?
				MIN_CAPACITY_INCREMENT : s >> 1)];
            System.arraycopy(a, 0, newArray, 0, s);
            array = a = newArray;
        }
        a[s] = object;
        size = s + 1;
        modCount++;
        return true;
    }
  
    @Override public void add(int index, E object) 
	{
        Object[] a = array;
        int s = size;
        if (index > s || index < 0) {
            throwIndexOutOfBoundsException(index, s);
        }
        if (s < a.length) {
            System.arraycopy(a, index, a, index + 1, s - index);
        } else {
            // assert s == a.length;
            Object[] newArray = new Object[newCapacity(s)];
            System.arraycopy(a, 0, newArray, 0, index);
            System.arraycopy(a, index, newArray, index + 1, s - index);
            array = a = newArray;
        }
        a[index] = object;
        size = s + 1;
        modCount++;
    }
  
    private static int newCapacity(int currentCapacity) {
        int increment = (currentCapacity < (MIN_CAPACITY_INCREMENT / 2) ?
			MIN_CAPACITY_INCREMENT : currentCapacity >> 1);
        return currentCapacity + increment;
    }

    @Override public boolean addAll(Collection<? extends E> collection) 
	{
        Object[] newPart = collection.toArray();
        int newPartSize = newPart.length;
        if (newPartSize == 0) {
            return false;
        }
        Object[] a = array;
        int s = size;
        int newSize = s + newPartSize; // If add overflows, arraycopy will fail
        if (newSize > a.length) {
            int newCapacity = newCapacity(newSize - 1);  // ~33% growth room
            Object[] newArray = new Object[newCapacity];
            System.arraycopy(a, 0, newArray, 0, s);
            array = a = newArray;
        }
        System.arraycopy(newPart, 0, a, s, newPartSize);
        size = newSize;
        modCount++;
        return true;
    }
  
    @Override
    public boolean addAll(int index, Collection<? extends E> collection) 
	{
        int s = size;
        if (index > s || index < 0) {
            throwIndexOutOfBoundsException(index, s);
        }
        Object[] newPart = collection.toArray();
        int newPartSize = newPart.length;
        if (newPartSize == 0) {
            return false;
        }
        Object[] a = array;
        int newSize = s + newPartSize; // If add overflows, arraycopy will fail
        if (newSize <= a.length) {
			System.arraycopy(a, index, a, index + newPartSize, s - index);
        } else {
            int newCapacity = newCapacity(newSize - 1);  // ~33% growth room
            Object[] newArray = new Object[newCapacity];
            System.arraycopy(a, 0, newArray, 0, index);
            System.arraycopy(a, index, newArray, index + newPartSize, s-index);
            array = a = newArray;
        }
        System.arraycopy(newPart, 0, a, index, newPartSize);
        size = newSize;
        modCount++;
        return true;
    }

    static IndexOutOfBoundsException throwIndexOutOfBoundsException(int index, int size) {
        throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + size);
    }
  
    @Override public void clear()
	{
        if (size != 0) {
            Arrays.fill(array, 0, size, null);
            size = 0;
            modCount++;
        }
    }

    public void ensureCapacity(int minimumCapacity)
	{
        Object[] a = array;
        if (a.length < minimumCapacity) {
            Object[] newArray = new Object[minimumCapacity];
            System.arraycopy(a, 0, newArray, 0, size);
            array = newArray;
            modCount++;
        }
    }
    @SuppressWarnings("unchecked") @Override public E get(int index) {
        if (index >= size) {
            throwIndexOutOfBoundsException(index, size);
        }
        return (E) array[index];
    }

	
    @Override public int size() {
        return size;
    }
    @Override public boolean isEmpty() {
        return size == 0;
    }
    @Override public boolean contains(Object object) 
	{
        Object[] a = array;
        int s = size;
        if (object != null) {
            for (int i = 0; i < s; i++) {
                if (a[i] == object) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < s; i++) {
                if (a[i] == null) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override public int indexOf(Object object) 
	{
        Object[] a = array;
        int s = size;
        if (object != null) {
            for (int i = 0; i < s; i++) {
                if (a[i] == object) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < s; i++) {
                if (a[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }
    @Override public int lastIndexOf(Object object)
	{
        Object[] a = array;
        if (object != null) {
            for (int i = size - 1; i >= 0; i--) {
                if (a[i] == object) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (a[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }
   
    @Override public E remove(int index) 
	{
        Object[] a = array;
        int s = size;
        if (index >= s) {
            throwIndexOutOfBoundsException(index, s);
        }
        @SuppressWarnings("unchecked") E result = (E) a[index];
        System.arraycopy(a, index + 1, a, index, --s - index);
        a[s] = null;  // Prevent memory leak
        size = s;
        modCount++;
        return result;
    }
    @Override public boolean remove(Object object)
	{
        Object[] a = array;
        int s = size;
        if (object != null) {
            for (int i = 0; i < s; i++) {
                if (a[i] == object) {
                    System.arraycopy(a, i + 1, a, i, --s - i);
                    a[s] = null;  // Prevent memory leak
                    size = s;
                    modCount++;
                    return true;
                }
            }
        } else {
            for (int i = 0; i < s; i++) {
                if (a[i] == null) {
                    System.arraycopy(a, i + 1, a, i, --s - i);
                    a[s] = null;  // Prevent memory leak
                    size = s;
                    modCount++;
                    return true;
                }
            }
        }
        return false;
    }
    @Override protected void removeRange(int fromIndex, int toIndex)
	{
        if (fromIndex == toIndex) {
            return;
        }
        Object[] a = array;
        int s = size;
        if (fromIndex >= s) {
            throw new IndexOutOfBoundsException("fromIndex " + fromIndex
												+ " >= size " + size);
        }
        if (toIndex > s) {
            throw new IndexOutOfBoundsException("toIndex " + toIndex
												+ " > size " + size);
        }
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex " + fromIndex
												+ " > toIndex " + toIndex);
        }
        System.arraycopy(a, toIndex, a, fromIndex, s - toIndex);
        int rangeSize = toIndex - fromIndex;
        Arrays.fill(a, s - rangeSize, s, null);
        size = s - rangeSize;
        modCount++;
    }
   
    @Override public E set(int index, E object)
	{
        Object[] a = array;
        if (index >= size) {
            throwIndexOutOfBoundsException(index, size);
        }
        @SuppressWarnings("unchecked") E result = (E) a[index];
        a[index] = object;
        return result;
    }
  
    @Override public Object[] toArray() 
	{
        int s = size;
        Object[] result = new Object[s];
        System.arraycopy(array, 0, result, 0, s);
        return result;
    }
    
    @Override public <T> T[] toArray(T[] contents)
	{
        int s = size;
        if (contents.length < s) {
            @SuppressWarnings("unchecked") T[] newArray
                = (T[]) Array.newInstance(contents.getClass().getComponentType(), s);
            contents = newArray;
        }
        System.arraycopy(this.array, 0, contents, 0, s);
        if (contents.length > s) {
            contents[s] = null;
        }
        return contents;
    }
   
    public void trimToSize() 
	{
        int s = size;
        if (s == array.length) {
            return;
        }
        if (s == 0) {
            array = EmptyArray.OBJECT;
        } else {
            Object[] newArray = new Object[s];
            System.arraycopy(array, 0, newArray, 0, s);
            array = newArray;
        }
        modCount++;
    }
	
    @Override public Iterator<E> iterator() {
        return new ArrayListIterator();
    }
    private class ArrayListIterator implements Iterator<E> 
	{
        /** Number of elements remaining in this iteration */
        private int remaining = size;
        /** Index of element that remove() would remove, or -1 if no such elt */
        private int removalIndex = -1;
        /** The expected modCount value */
        private int expectedModCount = modCount;
		
        public boolean hasNext() {
            return remaining != 0;
        }
        @SuppressWarnings("unchecked") public E next() 
		{
            IdentityArrayList<E> ourList = IdentityArrayList.this;
            int rem = remaining;
            if (ourList.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (rem == 0) {
                throw new NoSuchElementException();
            }
            remaining = rem - 1;
            return (E) ourList.array[removalIndex = ourList.size - rem];
        }
        public void remove() 
		{
            Object[] a = array;
            int removalIdx = removalIndex;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (removalIdx < 0) {
                throw new IllegalStateException();
            }
            System.arraycopy(a, removalIdx + 1, a, removalIdx, remaining);
            a[--size] = null;  // Prevent memory leak
            removalIndex = -1;
            expectedModCount = ++modCount;
        }
    }
	
}
