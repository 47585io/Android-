package com.editor.text2.base.share2;
import android.view.*;
import android.content.*;
import android.widget.*;

public interface ViewHolderFactory<T>
{
	public ViewHolder<T> newViewHodler()
	
	public static interface ViewHolder<T>
	{
		public View creatView(ViewGroup parent, WordAdapter<T> adapter, int position)

		public void resetView(ViewGroup parent, View convertView, WordAdapter<T> adapter, int position)
	}
}
