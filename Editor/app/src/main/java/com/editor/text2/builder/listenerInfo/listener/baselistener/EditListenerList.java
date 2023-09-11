package com.editor.text2.builder.listenerInfo.listener.baselistener;
import java.util.*;

/*
  EditListenerList，EditListener的子接口，可向上转型
  
  内部管理一组的EditListener
  
*/
public abstract interface EditListenerList extends EditListener
{
	
	public abstract int size();

    public abstract boolean contains(EditListener p1);

    public abstract boolean add(EditListener p1);

    public abstract boolean remove(EditListener p1);

    public abstract void clear();
	
	public abstract EditListener[] toArray()
	
}
