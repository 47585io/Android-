package com.editor.text2.base.share2;
import android.widget.*;

public class wordIcon3 implements wordIcon
{
	private int icon; //通过id加载图像
	private CharSequence name;
	
	public wordIcon3(){}
	public wordIcon3(int id,CharSequence name){
		this.icon=id;
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
	
	@Override
	public void loadImage(ImageView v)
	{
		v.setImageResource(icon);
	}

	@Override
	public void loadText(TextView v)
	{
		v.setText(name);
	}
	
}
