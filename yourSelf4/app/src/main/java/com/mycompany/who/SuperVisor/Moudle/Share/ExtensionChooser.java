package com.mycompany.who.SuperVisor.Moudle.Share;
import com.mycompany.who.Edit.ListenerVistor.*;
import java.util.*;

/*

 除Edit默认的Listener，

 新添加的Extension会直接添加到已经有的Edit

 之后添加一个Edit，直接赋予全部的Extension

 CompleteList和CanvaserList可以直接加

 Drawer之类的，将由用户选择一个默认的

 */
public class ExtensionChooser implements Extension.Extension_Spiltor
{

	@Override
	public void findExtension(String name)
	{
		
	}
	
	@Override
	public void addAExtension(com.mycompany.who.Edit.ListenerVistor.Extension E)
	{
		Extensions.add(E);
		giveAllInfoForAExtension(E);
	}

	@Override
	public void delAExtension(com.mycompany.who.Edit.ListenerVistor.Extension E)
	{
		if(Extensions.remove(E))
		    E.Delete();
	}
	
	List<Extension> Extensions;

	public ExtensionChooser(){
		Extensions = new ArrayList<>();
	}
	
	
	//给所有Extension一个Info
	public void giveAInfoForAllExtension(EditListenerInfo Info){
		for(Extension E:Extensions)
		    E.creatListener(Info);
	}
	//给一个Extension所有Info
	public void giveAllInfoForAExtension(Extension E){
		for(Extension e:Extensions){
			for(EditListenerInfo Info:e.getInfos()){
				E.creatListener(Info);
			}
		}
	}
	
}
