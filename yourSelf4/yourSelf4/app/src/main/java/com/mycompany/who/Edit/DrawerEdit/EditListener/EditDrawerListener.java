package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;
import android.text.*;

public abstract class EditDrawerListener extends EditListener
{
	abstract public void onDraw(int start, int end, ArrayList<wordIndex> nodes,Editable editor);
}
	
