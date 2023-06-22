package com.who.Edit.Base.Share.Share2;

import android.widget.*;

public class Icon3 implements Icon
{
	private int icon; //通过id加载图像
	private CharSequence name;

	public Icon3(){}
	public Icon3(int id,CharSequence name){
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
