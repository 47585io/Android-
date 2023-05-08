package com.mycompany.who.Edit.Base.Share.Share2;
import android.widget.*;
import android.graphics.*;

public class IconX implements Icon
{
	private int icon;
	private String path;
	private CharSequence name;//通过id或path加载图像

	public IconX(){}
	public IconX(int id,CharSequence name){
		this.icon=id;
		this.name=name;
	}
	public IconX(String path,CharSequence name){
		this.path=path;
		this.name=name;
	}
	public IconX(Icon3 I){
		this.icon=I.getIcon();
		this.name=I.getName();
	}
	public IconX(Icon2 I){
		this.path=I.getPath();
		this.name=I.getName();
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
		if(getPath()!=null)
		    v.setImageBitmap(BitmapFactory.decodeFile(getPath()));
		else
		    v.setImageResource(icon);
	}

	@Override
	public void loadText(TextView v)
	{
		v.setText(name);
	}
	
}
