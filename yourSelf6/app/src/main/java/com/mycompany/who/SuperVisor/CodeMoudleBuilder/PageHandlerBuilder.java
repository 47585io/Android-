package com.mycompany.who.SuperVisor.CodeMoudleBuilder;
import com.mycompany.who.SuperVisor.CodeMoudleBuilder.CodeMoudleBuilderInterface.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;

public abstract class PageHandlerBuilder implements PageHandlerBuilderInterface
{

	@Override
	public void ConfigSelf(PageHandler target)
	{
		target.setonTabListener(this);
	}

}
