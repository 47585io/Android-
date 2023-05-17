package com.mycompany.who.SuperVisor.CodeMoudleBuilder;
import com.mycompany.who.SuperVisor.CodeMoudleBuilder.CodeMoudleBuilderInterface.*;
import android.os.*;

public class senderObject implements sender
{
	
	Handler handler;

	@Override
	public void setHandler(Handler han)
	{
		handler = han;
	}

	@Override
	public Handler getHandler()
	{
		return handler;
	}

}
