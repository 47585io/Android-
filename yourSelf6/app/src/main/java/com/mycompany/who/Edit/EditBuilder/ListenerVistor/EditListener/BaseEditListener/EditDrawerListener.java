package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import java.util.*;

public abstract interface EditDrawerListener extends EditListener
{
	 public abstract void LetMeDraw(int start, int end, List<wordIndex> nodes,Spannable editor)
	
	 public abstract String getHTML(List<wordIndex> nodes,String text)
}
