package com.mycompany.who.SuperVisor.CodeMoudle;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;
import java.util.logging.*;


/*
  标题栏，
  
  我想完成ReSpinner切换编辑器的梦
  
  我想用一些按扭来封装编辑器功能
  
*/
public class Title extends HasAll 
{

	protected ReSpinner Spinner;
	protected LinearLayout ButtonBar;
	protected ReSpinner More;
	
	public Title(Context cont){
		super(cont);
	}
	public Title(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}
	@Override
	public void init()
	{
		Creator = new TitleCreator(R.layout.Title);
		Creator.ConfigSelf(this);
	}
	
	
	public ReSpinner getReSpinner(){
		return Spinner;
	}
	public LinearLayout getButtonBar(){
		return ButtonBar;
	}
	
	final public static class Config_hesSize extends Config_Size2<Title>
	{

		public size getSpinnerSize(){
			if(flag==Configuration.ORIENTATION_PORTRAIT)
				return new size((int)(width*0.6),height);
			else if(flag==Configuration.ORIENTATION_LANDSCAPE)
				return new size(width,(int)(height*0.6));
			return null;
		}
		public size getButtonBarSize(){
			if(flag==Configuration.ORIENTATION_PORTRAIT)
				return new size((int)(width*0.4),height);
			else if(flag==Configuration.ORIENTATION_LANDSCAPE)
				return new size(width,(int)(height*0.4));
			return null;
		}
		
		@Override
		public void onPort(Title target, int src)
		{
			super.onPort(target, src);
			trim(target.Spinner,(int)(width*0.6),height);
			trim(target.ButtonBar,(int)(width*0.4),height);
			trim(target,width,height);
			//竖屏，Title大小为width和height，然后分配给两个子元素一半的宽
			
			src = CastFlag(src);
			target.ButtonBar.setOrientation(src);
			target.setOrientation(src);
			//设置排列方向为LinearLayout.HORIZONTAL
			
			RotateViewFromPortToLand(target.Spinner);
			RotateViewFromPortToLand(target.ButtonBar);
			target.setPadding(0,0,0,20);
		}

		@Override
		public void onLand(Title target, int src)
		{
			super.onLand(target, src);
			
			//Spinner和ButtonBar先保持原来的样子，其它的可以改变大小
			trim(target.Spinner,(int)(height*0.6),width);
			trim(target.ButtonBar,(int)(height*0.4),width); 
			trim(target,width,height);
			//横屏，Title大小为改变后的width和height，然后分配给两个子元素一半的高
			
			RotateViewFromLandToPort(target.Spinner,(float)(height*0.6),width,(float)(height*0.4));
			RotateViewFromLandToPort(target.ButtonBar,(float)(height*0.4),width,-width);
			
			src = CastFlag(src);
			target.setOrientation(src);
			target.setPadding(0,0,20,0);
			//设置排列方向为LinearLayout.VERTICAL
		}
		public void RotateViewFromLandToPort(View v,float width,float height,float y){
			//绕中点旋转，这个可以画个图就好理解了，注意旋转后View的坐标轴也旋转了
			v.setPivotX(width - height/2);
			v.setPivotY(height/2);
			v.setRotation(-90);
			v.setTranslationX(-(width- height));
			v.setTranslationY(y);
			//将View移到左边，下面
		}
		public void RotateViewFromPortToLand(View v){
			//还原位置
			v.setRotation(0);
			v.setTranslationX(0);
			v.setTranslationY(0);
		}
		
		 /*
		 另一个方案
		 target.Spinner.setPivotX(width/2);
		 target.Spinner.setPivotY(width/2);
		 target.Spinner. setRotation(90);
		 target.ButtonBar.setTranslationY(height/2-width);
		 */
		
	}
	
	//一顿操作后，Title所有成员都分配好了空间
	final static class TitleCreator extends Creator<Title>
	{

		public TitleCreator(int i){
			super(i);
		}

		@Override
		public void init(Title target, View root)
		{
			target. Configer = new Config_Level();
			target.config = new Config_hesSize();
			target.Spinner = root.findViewById(R.id.ReSpinner);
			target.ButtonBar = root.findViewById(R.id.ButtonBar);
		    target.More = root.findViewById(R.id.More);
		}

	}

	// 如何配置View层次结构
	final static class Config_Level implements Level<Title>
	{

		@Override
		public void clearConfig(Title target)
		{
			// TODO: Implement this method
		}


		@Override
		public void config(Title target)
		{
		}

		@Override
		public void ConfigSelf(Title target)
		{
			//target.setBackgroundColor(0xff222222);
			target.Spinner.setBackground(null);
			target.More.setBackgroundResource(R.drawable.More);
		}
		
	}
	
}
