package com.editor.text2.base.share2;
import android.view.*;
import android.content.*;

public interface ViewHolderFactory<T>
{
	public ViewHolder<T> newViewHodler()
	
	public static interface ViewHolder<T>
	{
		public View creatView(ViewGroup parent,T date,int position)

		public void resetView(ViewGroup parent,View root,T date,int position)
	}
}
