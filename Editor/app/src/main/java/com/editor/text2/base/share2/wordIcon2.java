package com.editor.text2.base.share2;
import android.graphics.*;
import android.widget.*;

public class wordIcon2 implements wordIcon
{
	private String path; //通过文件路径加载图像
	private CharSequence name; 
	
	public wordIcon2(){}
	public wordIcon2(String path,CharSequence name){
		this.path=path;
		setName(name);
	}

	public CharSequence getName() {
        return name;
    }
    public void setName(CharSequence name) {
        this.name = name;
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
	}
	@Override
	public void loadText(TextView v)
	{
		v.setText(name);
	}
	
}
