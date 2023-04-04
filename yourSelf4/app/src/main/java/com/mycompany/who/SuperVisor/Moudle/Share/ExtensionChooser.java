package com.mycompany.who.SuperVisor.Moudle.Share;
import java.util.*;
import java.security.cert.*;


/*

 除Edit默认的Listener，

 新添加的Extension会直接添加到已经有的Edit

 之后添加一个Edit，直接赋予全部的Extension

 CompleteList和CanvaserList可以直接加

 Drawer之类的，将由用户选择一个默认的


 一般地，一个Extension应该实现一个Runner和其对应的Listener，这由具体的Extension类决定

 基类Extension拥有获取Listener和Runner的抽象方法，并且提供禁用和删除功能

 Extension分配的Listener和Runner实例的hashCode将被记录在列表中

 若删除一个Extension，应该可以通过hashCode去所有编辑器中找，然后删除

 若禁用一个Extension，应该可以通过hashCode去所有编辑器中找，然后将它的Enabled属性赋值false

 若启用一个Extension，其和禁用做相反操作

 */
public class ExtensionChooser
{
	List<Extension> Extensions;

	public ExtensionChooser(){
		Extensions = new ArrayList<>();
	}
	
	
	
}
