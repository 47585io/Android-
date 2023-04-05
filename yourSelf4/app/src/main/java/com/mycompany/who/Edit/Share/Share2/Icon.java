package com.mycompany.who.Edit.Share.Share2;

public class Icon{
	
	private int icon;
	private String path; //通过icon或文件路径加载图像
	private CharSequence name; //支持Span文本
	private byte flag;
	
	public Icon(){

	}
	public Icon(int resid,CharSequence name){
		this.icon=resid;
		this.name=name;
	}
	public Icon(String path,CharSequence name){
		this.path=path;
		this.name=name;
	}
	
	public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int resid) {
        this.icon = resid;
    }
	
	public int getflag() {
        return flag;
    }

    public void setflag(int flag) {
        this.flag= (byte) flag;
    }


	public void setPath(String path){
		this.path=path;
	}
	public String getPath(){
		return path;
	}
	
	
}

