package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share;
import android.view.View.*;
import android.view.*;

public abstract class onTouchToZoom implements OnTouchListener 
{
	
	public int id1,id2,index1,index2;
	public float hp1x,hp1y,hp2x,hp2y;
	public float p1x,p1y,p2x,p2y;
	public float len,hlen;
	
	abstract public void onNarrow(View p1, MotionEvent p2,float bili);
	abstract public void onAmplification(View p1, MotionEvent p2,float bili);
	abstract public boolean onMoveEnd(View p1,MotionEvent p2,float bili);
	
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		float bili = 1;
	    if (p2.getPointerCount() == 2)
		{
			if(p2.getHistorySize()==0){
				//初始化
				id1=p2.getPointerId(0);
				id2=p2.getPointerId(1);
				hp1x=p2.getX(0);
				hp1y=p2.getY(0);
				hp2x=p2.getX(1);
				hp2y=p2.getY(1);
				hlen = (float)(Math.pow(hp1x-hp2x,2)+Math.pow(hp1y-hp2y,2));
				//原两点间的距离
			}
			else{
				index1=p2.findPointerIndex(id1);
				index2=p2.findPointerIndex(id2);
				//原来的手指上升了，切换为新的手指
				if(index1==-1){
					index1=index2==0 ? 1:0;
					id1=p2.getPointerId(index1);
				}
				if(index2==-1){
					index2=index1==0 ? 1:0;
					id2=p2.getPointerId(index2);
				}
				
				p1x=p2.getX(index1);
				p1y=p2.getY(index1);
				p2x=p2.getX(index2);
				p2y=p2.getY(index2);
				
				//新两点间的距离
				len = (float) (Math.pow(p1x-p2x,2)+Math.pow(p1y-p2y,2));
				bili = len/hlen;
				if(len>hlen)
				    onAmplification(p1,p2,bili);
				else
				    onNarrow(p1,p2,bili);
		    }
			hp1x=p1x;
			hp1y=p1y;
			hp2x=p2x;
			hp2y=p2y;
			hlen=len;
		}
		return onMoveEnd(p1,p2,bili);
	}
	
	public float Iszoom(){
		return len/hlen;
	}
	
	/* 未预料的缩放，在多个手指时下标会出现异常 */
	public static float Iszoom(MotionEvent p2)
	{
		float hp1x,hp1y,hp2x,hp2y;
	    float p1x,p1y,p2x,p2y;
		float len,hlen;
		int size = p2.getHistorySize();
		int bili = 1;
		
		if (p2.getPointerCount() == 2 && p2.getHistorySize() ==2)
		{
			p1x=p2.getX(0);
			p1y=p2.getY(0);
			p2x=p2.getX(1);
			p2y=p2.getY(1);
			
			hp1x= p2.getHistoricalX(0,size-1);
			hp1y= p2.getHistoricalY(0,size-1);
			hp2x= p2.getHistoricalX(1,size-1);
			hp2y= p2.getHistoricalY(1,size-1);
			
			len = (float) (Math.pow(p1x-p2x,2)+Math.pow(p1y-p2y,2));
			hlen = (float)( Math.pow(hp1x-hp2x,2)+Math.pow(hp1y-hp2y,2));
			return len/hlen;
		}
		return bili;
	}
	
	
	public static abstract interface onTouchToZoom extends OnTouchListener{
		
		public abstract void onNarrow(View p1, MotionEvent p2,float bili);
		
		public abstract void onAmplification(View p1, MotionEvent p2,float bili);
		
		public abstract boolean onMoveEnd(View p1,MotionEvent p2,float bili);
	
	}
	
}
