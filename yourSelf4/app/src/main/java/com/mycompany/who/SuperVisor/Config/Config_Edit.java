package com.mycompany.who.SuperVisor.Config;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.ListenerVistor.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Base.Edit.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.Share.Share4.*;

public class Config_Edit implements Configer<CodeEdit>
{
	
	public static Config_Edit configEdit;
	static{
		configEdit=new Config_Edit();
	}
	
	EditListenerInfo Info;
	ThreadPoolExecutor pool;
	EditLine lines;
	OtherWords Wordlib;
	
	@Override
	public void ConfigSelf(CodeEdit target)
	{
		target.setPool(pool);
		target.lines=lines;
		target.setInfo(Info);
		Getter.setFiled("WordLib",target,Wordlib);
	}
	
	public void set(EditListenerInfo Info,ThreadPoolExecutor pool,EditLine lines,OtherWords lib){
		this. Info=Info;
		this.pool=pool;
		this.lines=lines;
		Wordlib=lib;
	}
	
}
