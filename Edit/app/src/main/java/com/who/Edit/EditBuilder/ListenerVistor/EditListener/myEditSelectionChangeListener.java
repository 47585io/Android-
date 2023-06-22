package com.who.Edit.EditBuilder.ListenerVistor.EditListener;

import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import android.util.*;
import android.text.*;


/*
  光标变化监听器，应该保证编辑器光标可以变化
  
  selStart和selEnd表示光标变化后的位置，editor表示编辑器文本

*/
public abstract class myEditSelectionChangeListener extends myEditListener implements EditSelectionListener
{
	@Override
	public abstract void onSelectionChanged(int selStart, int selEnd, Spannable editor)
}
