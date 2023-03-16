package com.mycompany.who.Share;
import android.view.View.*;
import android.view.*;

public abstract class onTouchToZoom implements OnTouchListener 
{
	
	abstract boolean onNarrow(View p1, MotionEvent p2);
	abstract boolean onAmplification(View p1, MotionEvent p2);
	
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		if (p2.getPointerCount() == 2 && p2.getHistorySize() != 0)
		{
			if (
				(
				Math.sqrt(
					(
					Math.pow(
						Math.abs(p2.getX(0) - p2.getX(1)), 2
					)
					+
					Math.pow(
						Math.abs(p2.getY(0) - p2.getY(1)), 2
					)
					)
				)
				>
				(
				Math.sqrt(
					Math.pow(
						Math.abs(p2.getHistoricalX(0, p2.getHistorySize() - 1) - p2.getHistoricalX(1, p2.getHistorySize() - 1)), 2
					)		
					+
					Math.pow( 
						Math.abs(p2.getHistoricalY(0, p2.getHistorySize() - 1) - p2.getHistoricalY(1, p2.getHistorySize() - 1)), 2)
				)
				)
				)
				)		
			    return onAmplification(p1,p2);
			}
			 
			return onNarrow(p1,p2);
		
	}
	
}
