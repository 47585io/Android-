package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import android.view.*;


/*
 getWidth()和getHeight()获取的是自己在父View画布中的画框大小，setLeft(),setRight(),setTop(),setBottom(),setX(),sety()都是将自己放在父View中的指定位置
 
 但自己内部的画布可以无限延伸和滚动，例如setScale(),setRotate(),setTranstion()，它们都是将自己的画布缩放，旋转，平移了
 
 父View负责要求孑View绘画，子View要求绘制自己，子View在自己画布上绘制，父View负责将子View的画框范围的图像放到自己画布上


 View和canvas并不是同一个东西，可以这样想象，我们的View是固定的，相当于与一个画板或者画框，画板范围就是View的矩形范围，canvas是画布，真的就是一块布，我们在画布上画东西，最后呈现到画板(也就是View上)
 
 一开始View和Canvas的坐标是对齐的，也就是画布的右上角就是画框的右上角，四条边都是对齐的，然后canvas是可以变换的，我们平移canvas dx，dy，然后draw画图，就相当于把画布拉着往画框外拖动一段距离，然后作画，作画的时候坐标系是按着画布来的，跟View没有关系

 最后，最后画布上的东西按照画布的位置直接映射到View画板上，就是移动了的图像，同样的旋转就相当于把画布上的图转动了，默认原点，也可设置点，对于缩放，要理解一下，就是画的图以某个点为中心放大或者缩小

 
 一个小技巧：我们思考这几个变换叠加起来的效果的时候，总是容易按照api的顺序，先思考画布变化之后，图像怎么画上去，有时候很难想，感觉这是Android api先变画布，在作画这种顺序设置的局限吧，应该倒过来想才对，是图像画到画布上去之后，把画布进行变化，这样想起来简单多了

 (不过要注意的是，canvas的变换是影响后面draw的图(导致坐标系变换)，不影响前面画好的的图，也是是canva调用draw的时候，相当于画版就把画取走了，后面的canvas是一张白布，所有的图像在View这个画框上面叠加，画框外的图是看不到的

 */
public abstract interface Scroll
{

	public abstract void setCanScroll(boolean can)

	public abstract void setCanSave(boolean can)

	public abstract void setTouchInter(boolean can)
	
	public abstract void setzoomListener(onTouchToZoom zoom)
	
	public abstract void goback()

	public abstract void gonext()
	
	public abstract int size()
	
	public abstract int isScrollToEdge()
	
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
	
