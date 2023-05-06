package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import java.util.*;

public interface EditDrawerListener extends EditListener
{
	 public void LetMeDraw(int start, int end, List<wordIndex> nodes,Editable editor)
	
	 public String getHTML(List<wordIndex> nodes,String text)
	 
	 public String getHTML(Spanned b)
}
