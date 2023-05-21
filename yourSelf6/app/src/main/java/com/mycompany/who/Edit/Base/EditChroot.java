package com.mycompany.who.Edit.Base;
import com.mycompany.who.Edit.Base.*;

public class EditChroot implements EditMoudle.EditState
{
	
	private int mPrivateFlags;

	@Override
	public int getEditFlags()
	{
		return mPrivateFlags;
	}
	@Override
	public void setEditFlags(int flags)
	{
		mPrivateFlags = flags;
	}
	
	public void set(boolean isModify,boolean isUR,boolean isDraw,boolean isFormat,boolean isComplete,boolean isCanvas,boolean isRun,boolean isLine,boolean isSelection)
	{
	    IsModify(isModify);
		IsUR(isUR);
		IsDraw(isDraw);
		IsFormat(isFormat);
		IsComplete(isComplete);
		IsCanvas(isCanvas);
		IsRun(isRun);
		IsLine(isLine);
		IsSelection(isSelection);
	}
	public void set(EditMoudle.EditState s)
	{
		mPrivateFlags = s.getEditFlags();
	}
	
	public static void IsModify(int flag,boolean is){
		flag = is ? flag|ModifyMask : flag&~ModifyMask;
	}
	public static void IsUR(int flag,boolean is){
		flag = is ? flag|URMask : flag&~URMask;
	}
	public static void IsDraw(int flag,boolean is){
		flag = is ? flag|DrawMask : flag&~DrawMask;
	}
	public static void IsFormat(int flag,boolean is){
		flag = is ? flag|FormatMask : flag&~FormatMask;
	}
	public static void IsComplete(int flag,boolean is){
		flag = is ? flag|CompleteMask : flag&~CompleteMask;
	}
	public static void IsCanvas(int flag,boolean is){
		flag = is ? flag|CanvasMask : flag&~CanvasMask;
	}
	public static void IsRun(int flag,boolean is){
		flag = is ? flag|RunMask : flag&~RunMask;
	}
	public static void IsSelection(int flag,boolean is){
		flag = is ? flag|SelectionMask : flag&~SelectionMask;
	}
	public static void IsLine(int flag,boolean is){
		flag = is ? flag|LineMask : flag&~LineMask;
	}
	
	public static boolean IsModify(int flag){
		return (flag&ModifyMask) == ModifyMask;
	}
	public static boolean IsUR(int flag){
		return (flag&URMask) == URMask;
	}
	public static boolean IsDraw(int flag){
		return (flag&DrawMask) == DrawMask;
	}
	public static boolean IsFormat(int flag){
		return (flag&FormatMask) == FormatMask;
	}
	public static boolean IsComplete(int flag){
		return (flag&CompleteMask) == CompleteMask;
	}
	public static boolean IsCanvas(int flag){
		return (flag&CanvasMask) == CanvasMask;
	}
	public static boolean IsRun(int flag){
		return (flag&RunMask) == RunMask;
	}
	public static boolean IsSelection(int flag){
		return (flag&SelectionMask) == SelectionMask;
	}
	public static boolean IsLine(int flag){
		return (flag&LineMask) == LineMask;
	}
	
	public void IsModify(boolean is){
		mPrivateFlags = is ? mPrivateFlags|ModifyMask : mPrivateFlags&~ModifyMask;
	}
	public void IsUR(boolean is){
		mPrivateFlags = is ? mPrivateFlags|URMask : mPrivateFlags&~URMask;
	}
	public void IsDraw(boolean is){
		mPrivateFlags = is ? mPrivateFlags|DrawMask : mPrivateFlags&~DrawMask;
	}
	public void IsFormat(boolean is){
		mPrivateFlags = is ? mPrivateFlags|FormatMask : mPrivateFlags&~FormatMask;
	}
	public void IsComplete(boolean is){
		mPrivateFlags = is ? mPrivateFlags|CompleteMask : mPrivateFlags&~CompleteMask;
	}
	public void IsCanvas(boolean is){
		mPrivateFlags = is ? mPrivateFlags|CanvasMask : mPrivateFlags&~CanvasMask;
	}
	public void IsRun(boolean is){
		mPrivateFlags = is ? mPrivateFlags|RunMask : mPrivateFlags&~RunMask;
	}
	public void IsSelection(boolean is){
		mPrivateFlags = is ? mPrivateFlags|SelectionMask : mPrivateFlags&~SelectionMask;
	}
	public void IsLine(boolean is){
		mPrivateFlags = is ? mPrivateFlags|LineMask : mPrivateFlags&~LineMask;
	}

	public boolean IsModify(){
		return (mPrivateFlags&ModifyMask) == ModifyMask;
	}
	public boolean IsUR(){
		return (mPrivateFlags&URMask) == URMask;
	}
	public boolean IsDraw(){
		return (mPrivateFlags&DrawMask) == DrawMask;
	}
	public boolean IsFormat(){
		return (mPrivateFlags&FormatMask) == FormatMask;
	}
	public boolean IsComplete(){
		return (mPrivateFlags&CompleteMask) == CompleteMask;
	}
	public boolean IsCanvas(){
		return (mPrivateFlags&CanvasMask) == CanvasMask;
	}
	public boolean IsRun(){
		return (mPrivateFlags&RunMask) == RunMask;
	}
	public boolean IsSelection(){
		return (mPrivateFlags&SelectionMask) == SelectionMask;
	}
	public boolean IsLine(){
		return (mPrivateFlags&LineMask) == LineMask;
	}
	
}

/*
class tmp{
	
	private EditChroot root;
	
	public void IsModify(boolean is){
		root.IsModify(is);
	}
	public void IsUR(boolean is){
		root.IsUR(is);
	}
	public void IsDraw(boolean is){
		root.IsDraw(is);
	}
	public void IsFormat(boolean is){
		root.IsFormat(is);
	}
	public void IsComplete(boolean is){
		root.IsComplete(is);
	}
	public void IsCanvas(boolean is){
		root.IsCanvas(is);
	}
	public void IsRun(boolean is){
		root.IsRun(is);
	}
	public void IsSelection(boolean is){
		root.IsSelection(is);
	}
	public void IsLine(boolean is){
		root.IsLine(is);
	}

	public boolean IsModify(){
		return root.IsModify();
	}
	public boolean IsUR(){
		return root.IsUR();
	}
	public boolean IsDraw(){
		return root.IsDraw();
	}
	public boolean IsFormat(){
		return root.IsFormat();
	}
	public boolean IsComplete(){
		return root.IsComplete();
	}
	public boolean IsCanvas(){
		return root.IsCanvas();
	}
	public boolean IsRun(){
		return root.IsRun();
	}
	public boolean IsSelection(){
		return root.IsSelection();
	}
	public boolean IsLine(){
		return root.IsLine();
	}
	
}
*/
