package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;

public abstract class EditDrawerListener extends EditListener
{
	final public void LetMeDraw(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder, Editable editor)
	{
		try
		{
			if (Enabled())
				Draw(start,end,nodes,builder,editor);
		}
		catch (Exception e)
		{
			Log.e("Drawing Error", toString()+" "+e.toString());
		}
	}

	protected void Draw(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder, Editable editor){
		onDraw(start, end, nodes,builder, editor);
	}
	abstract public void onDraw(final int start, final int end, List<wordIndex> nodes, SpannableStringBuilder builder, Editable editor)
	
}
