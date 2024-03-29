package com.mycompany.who.SuperVisor.CodeMoudle.Base.View;

import android.content.*;
import android.util.*;
import android.widget.*;

public class ReSpinner extends  Spinner
 {
	public boolean isDropDownMenuShown=false;//标志下拉列表是否正在显示
    private onSelectionListener slistener=null;
	
	public ReSpinner(Context context) {
		super(context);
	}

	public ReSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ReSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setonSelectionListener(onSelectionListener li){
		slistener=li;
	}

	@Override
	public void setSelection(int position, boolean animate) {
		boolean sameSelected = position == getSelectedItemPosition();
		super.setSelection(position, animate);
		if (sameSelected) {
			// 如果选择项是Spinner当前已选择的项,则 OnItemSelectedListener并不会触发,因此这里手动触发回调
			//getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
			if(slistener!=null)
				slistener.onRepeatSelected(position);
		}
	}

	@Override
	public boolean performClick() {
		this.isDropDownMenuShown = true;
		return super.performClick();
	}

	public boolean isDropDownMenuShown(){
		return isDropDownMenuShown;
	}

	public void setDropDownMenuShown(boolean isDropDownMenuShown){
		this.isDropDownMenuShown=isDropDownMenuShown;
	}

	@Override
	public void setSelection(int position) {
		boolean sameSelected = position == getSelectedItemPosition();
		super.setSelection(position);
		if (sameSelected) {
			//getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
			if(slistener!=null)
				slistener.onRepeatSelected(position);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public static abstract interface onSelectionListener
	{
		public abstract void onRepeatSelected(int postion)
	}
	
}


