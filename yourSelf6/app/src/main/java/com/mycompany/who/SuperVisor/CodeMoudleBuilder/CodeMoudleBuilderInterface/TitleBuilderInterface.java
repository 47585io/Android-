package com.mycompany.who.SuperVisor.CodeMoudleBuilder.CodeMoudleBuilderInterface;

import com.mycompany.who.Edit.Base.EditMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import android.widget.AdapterView.*;
import android.widget.*;


public interface TitleBuilderInterface extends Configer<Title>,ReSpinner.onSelectionListener,OnItemSelectedListener,sender
{
	public void loadButton(LinearLayout ButtonBar)
	
	public void onMenuCreat(AdapterView v)

	public void onMenuItemSelected(AdapterView v,int pos)
	
	public static interface ButtonClickFactory{}
}
