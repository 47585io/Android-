package com.mycompany.who.Share;
import android.view.View.*;
import android.view.*;

public abstract class onTouchToZoom implements OnTouchListener 
{
	
	public int id1,id2,index1,index2;
	public float hp1x,hp1y,hp2x,hp2y;
	public float p1x,p1y,p2x,p2y;
	
	abstract public void onNarrow(View p1, MotionEvent p2);
	abstract public void onAmplification(View p1, MotionEvent p2);
	abstract public boolean onMoveEnd(View p1,MotionEvent p2);
	
	public static final boolean onAmplification = true;
	public static final boolean onNarrow = false;
	
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
	    if (p2.getPointerCount() == 2)
		{
			if(p2.getHistorySize()==0){
				id1=p2.getPointerId(0);
				id2=p2.getPointerId(1);
				hp1x=p2.getX(0);
				hp1y=p2.getY(0);
				hp2x=p2.getX(1);
				hp2y=p2.getY(1);
			}
			else{
				index1=p2.findPointerIndex(id1);
				index2=p2.findPointerIndex(id2);
				p1x=p2.getX(index1);
				p1y=p2.getY(index1);
				p2x=p2.getX(index2);
				p2y=p2.getY(index2);
				
				if(Math.pow(p1x-p2x,2)+Math.pow(p1y-p2y,2)
				  >Math.pow(hp1x-hp2x,2)+Math.pow(hp1y-hp2y,2))
				    onAmplification(p1,p2);
				else
				    onNarrow(p1,p2);
		    }
			hp1x=p1x;
			hp1y=p1y;
			hp2x=p2x;
			hp2y=p2y;
		}

		return onMoveEnd(p1,p2);
	}
	
	public static boolean Iszoom(MotionEvent p2){
		
		if (p2.getPointerCount() == 2 && p2.getHistorySize() != 0)
		{
			if (
				(
				Math.sqrt(
					(
					Math.pow(
						p2.getX(0) - p2.getX(1), 2
					)
					+
					Math.pow(
						p2.getY(0) - p2.getY(1), 2
					)
					)
				)
				>
				(
				Math.sqrt(
					Math.pow(
						p2.getHistoricalX(0, p2.getHistorySize() - 1) - p2.getHistoricalX(1, p2.getHistorySize() - 1), 2)		
					+
					Math.pow( 
						p2.getHistoricalY(0, p2.getHistorySize() - 1) - p2.getHistoricalY(1, p2.getHistorySize() - 1), 2))
				    )
				)
				)		
			    return onAmplification;
		}

		return onNarrow;
		
	}
	
}
