package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import android.view.*;

public interface Scroll{

	public void setCanScroll(boolean can)

	public void setCanSave(boolean can)

	public void setTouchInter(boolean can)
	
	public void goback()

	public void gonext()
	
	public int size()
	
	public int isScrollToEdge()
	
	
	public static class NoThingScroll extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			// TODO: Implement this method
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			// TODO: Implement this method
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			// TODO: Implement this method
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			// TODO: Implement this method
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			// TODO: Implement this method
			return false;
		}
	}
	
}
	
