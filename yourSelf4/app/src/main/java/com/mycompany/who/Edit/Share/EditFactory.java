package com.mycompany.who.Edit.Share;
import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import com.mycompany.who.Edit.*;
import java.util.concurrent.*;

public abstract class EditFactory
{
	public static DrawerEnd GetDrawerEdit(Context cont,String Lua){
		DrawerEnd Edit= new DrawerEnd(cont);
		Edit.setLuagua(Lua);
		Edit.setRunner(EditRunnerFactory.getDrawerRunner());
		return Edit;
	}
	public static FormatEdit GetFormatEdit(Context cont,String Lua){
		FormatEdit Edit= new FormatEdit(cont);
		Edit.setLuagua(Lua);
		Edit.setRunner(EditRunnerFactory.getFormatRunner());
		return Edit;
	}
	public static CompleteEdit GetCompleteEdit(Context cont,String Lua,ThreadPoolExecutor pool){
		CompleteEdit Edit=new CompleteEdit(cont);
		Edit.setLuagua(Lua);
		Edit.setRunner(EditRunnerFactory.getCompleteRunner());
		Edit.setPool(pool);
		return Edit;
	}
	public static CoCoEdit GetCanvasEdit(Context cont,String Lua,ThreadPoolExecutor pool){
		CoCoEdit Edit= new CoCoEdit(cont);
		Edit.setLuagua(Lua);
		Edit.setRunner(EditRunnerFactory.getCanvasRunner());
		Edit.setPool(pool);
		return Edit;
	}
	
	public static DrawerEnd GetFormEdit(DrawerEnd Edit){
		return new DrawerEnd(Edit.getContext(),Edit);
	}
	public static FormatEdit GetFormEdit(FormatEdit Edit){
		return new FormatEdit(Edit.getContext(),Edit);
	}
	public static CompleteEdit GetFormEdit(CompleteEdit Edit){
		return new CompleteEdit(Edit.getContext(),Edit);
	}
	public static CoCoEdit GetFormEdit(CoCoEdit Edit){
		return new CoCoEdit(Edit.getContext(),Edit);
	}
	
	public abstract CodeEdit GetCodeEdit(Context cont,String Lua)

	public abstract CodeEdit GetFormEdit(CodeEdit Edit)
	
	
}
