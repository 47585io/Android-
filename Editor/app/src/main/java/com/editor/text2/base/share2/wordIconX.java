package com.editor.text2.base.share2;
import android.graphics.*;
import android.widget.*;
import android.app.*;
import android.view.*;
import android.widget.FrameLayout.*;

public class wordIconX implements wordIcon
{
	private int icon;
	private String path;
	private CharSequence name;//通过id或path加载图像

	public wordIconX(){}
	public wordIconX(int id,CharSequence name){
		this.icon=id;
		this.name=name;
	}
	public wordIconX(String path,CharSequence name){
		this.path=path;
		this.name=name;
	}
	
	public CharSequence getName() {
        return name;
    }
    public void setName(CharSequence name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }
    public void setIcon(int id) {
        this.icon = id;
    }
	
	public void setPath(String path){
		this.path=path;
	}
	public String getPath(){
		return path;
	}
	
	@Override
	public void loadImage(ImageView v)
	{
		if(path!=null)
		    v.setImageBitmap(BitmapFactory.decodeFile(getPath()));
		else
		    v.setImageResource(icon);
		Display display = ((Activity)v.getContext()).getWindowManager().getDefaultDisplay();
		ViewGroup.LayoutParams parms = v.getLayoutParams();
		parms.width = display.getHeight()*6/100;
		parms.height = parms.width;
	}

	@Override
	public void loadText(TextView v)
	{
		v.setText(name);
	}
	
}
