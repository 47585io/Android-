package com.mycompany.who.SuperVisor.Config;
import android.view.*;
import com.mycompany.who.SuperVisor.*;

public class ConfigViewWith_PortAndLand implements Configer<View>
{
	public boolean portOrLand;
	public int width;
	public int height;
	
	public static final boolean Port=true;
	public static final boolean Land=false;
	
	@Override
	public void ConfigSelf(View target)
	{
		if(portOrLand==Port){
			EditGroup.trim(target,width,height);
		}
		else
			EditGroup.trim(target,height,width);
	}
	
	public void set(int width,int height,boolean is){
		this.width=width;
		this.height=height;
		portOrLand=is;
	}
	
	
}
