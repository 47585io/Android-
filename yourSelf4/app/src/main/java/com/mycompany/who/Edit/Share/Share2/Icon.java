package com.mycompany.who.Edit.Share.Share2;

public class Icon{
	
	private int icon;
	private String path;
	private String name;
	private byte flag=-128;
	
	public Icon(){

	}
	public Icon(int resid,String name){
		this.icon=resid;
		this.name=name;
	}
	public Icon(String path,String name){
		this.path=path;
		this.name=name;
	}
	
	public String getName() {
        return name;
    }

    public void setName(String name) {
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

