package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import java.util.*;

public abstract class EditDrawerListener extends EditListener
{
	abstract public void onDrawNodes(final int start, final int end,String src, List<wordIndex> nodes, EditText self)
	
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
		onDrawNodes(start, end,src, nodes, self);
	}
	public Colors.ByteToColor2 BToC = null;

	public Colors.ByteToColor2 getByteToColor(){
		return BToC;
	}
	public void setSpan(int start,int end,Editable b,List<wordIndex> nodes){
		Colors.ForeColorText(b,nodes,start,getByteToColor());
	}
	public void clearSpan(int start,int end,Editable b){
		Colors.clearSpan(start,end,b,Colors.ForeSpanType);
	}
}
