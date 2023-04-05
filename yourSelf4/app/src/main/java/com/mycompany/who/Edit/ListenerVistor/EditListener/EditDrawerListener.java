package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import com.mycompany.who.Edit.Share.Share1.*;
import java.util.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;

public abstract class EditDrawerListener extends EditListener
{
	abstract public void onDraw(final int start, final int end,String src, List<wordIndex> nodes, EditText self)
	
	final public void LetMeDraw(int start, int end,String src, List<wordIndex> nodes,EditText self)
	{
		try
		{
			if (Enabled())
				Draw(start,end,src,nodes,self);
		}
		catch (Exception e)
		{
			Log.e("Drawing Error", toString()+" "+e.toString());
		}
	}

	protected void Draw(int start, int end,String src, List<wordIndex> nodes,EditText self){
		onDraw(start, end,src, nodes, self);
	}
	
	public Colors.ByteToColor2 BToC = null;

	public Colors.ByteToColor2 getByteToColor(){
		return BToC;
	}
	
	public void setSpan(SpannableStringBuilder b,List<wordIndex> nodes){
		wordIndex[] tmp = new wordIndex[nodes.size()];
		nodes.toArray(tmp);
		Colors.ForeColorText(b,tmp,0,getByteToColor());
	}
	
}
