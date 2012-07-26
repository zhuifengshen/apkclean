package com.rodrigo.apkclean;

public class DeviceInfo {
	public static enum ScreenSize{
		small,normal,large,xlarge
	} 
	public static enum ScreenAspect	{
		llong,notlong;
		@Override
		public String toString() {
			if (this.equals(ScreenAspect.llong))
				return "long";
			
			return super.toString();
		}
	}
	public static enum  PixelDensity {
		ldpi,mdpi,hdpi,xhdpi,nodpi,tvdpi
	}
	
	private int width=240;
	private int height=320;
	private int dpi=120;
	private int versionapi=7;
	
	int sw,wdp,hdp;
	
	ScreenSize size;
	ScreenAspect aspect;
	PixelDensity density;
	
	
	boolean touch=true;//Device has a touchscreen
	boolean querty=false;//has a phisical querty
	boolean key12=false;//has a phisical 12 key keyboard
	boolean dpad=false;//has a dpad
	boolean track=false;//has a trackball
	boolean whell=false;//has a whell
	
	public DeviceInfo() {

	}
			
	public void setWidth(int width) {
		this.width = width;
		processSize();
	}

	public void setHeight(int height) {
		this.height = height;
		processSize();
	}
	
	public void processSize(){
		int hh = width<=height? width : height;
		this.hdp =  (int) ((height * 160f) / (float)dpi);//height
		this.wdp =  (int) ((width * 160f) / (float)dpi);//width
		
		this.sw = (int) ((hh * 160f) / (float)dpi);//smalest width
		
		int area = this.wdp * this.hdp;
		if (area<150400)      size = ScreenSize.small;  //small  pelo menos 426dp x 320dp = 136320
		else if (area<307200) size = ScreenSize.normal; //normal pelo menos 470dp x 320dp = 150400
		else if (area<691200) size = ScreenSize.large;  //large  pelo menos 640dp x 480dp = 307200
		else                  size = ScreenSize.xlarge; //xlarge pelo menos 960dp x 720dp = 691200
		
		if (((float)width/(float)height) >= (5f/3f) || ((float)height/(float)width) >= (5f/3f))
			aspect = ScreenAspect.llong;
		else
			aspect = ScreenAspect.notlong;
	}
	
	public void setDpi(int dpi) {
		this.dpi = dpi;
		processSize();
		
		if (dpi<140)      density = PixelDensity.ldpi;  //320dpi -xhdpi 
		else if (dpi<187) density = PixelDensity.mdpi;  //240dpi -hdpi
		else if (dpi<227) density = PixelDensity.tvdpi; //213dpi -tvdpi
		else if (dpi<280) density = PixelDensity.hdpi;  //160dpi -mdpi
		else              density = PixelDensity.xhdpi; //120dpi -ldpi
		
	}

	public void setVersionapi(int versionapi) {
		this.versionapi = versionapi;
	}

	public boolean CanUse(Resource res){
		boolean ret = true;
	
		for (PropTag tag: res.prop_tags){
			if (tag.getName().matches("sw[0-9]+dp")){
				if (this.sw<tag.getSubindex())
					return false;
				else
					ret = true;
			}else if (tag.getName().matches("w[0-9]+dp")){
				if (this.wdp<tag.getSubindex())
					return false;
				else
					ret = true;
			}else if (tag.getName().matches("h[0-9]+dp")){
				if (this.hdp<tag.getSubindex())
					return false;
				else
					ret = true;
			}else if (tag.getName().matches("small")){
				if (this.size.equals(ScreenSize.small))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("normal")){
				if (this.size.equals(ScreenSize.normal))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("large")){
				if (this.size.equals(ScreenSize.large))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("xlarge")){
				if (this.size.equals(ScreenSize.xlarge))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("long")){
				if (this.aspect.equals(ScreenAspect.llong))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("notlong")){
				if (this.aspect.equals(ScreenAspect.notlong))
					ret = true;
				else
					return false;
			}else if (tag.getName().matches("[0-9]+dpi")){
				if (this.dpi<tag.getSubindex())
					return false;
				else
					ret = true;
			}
			else if (tag.getName().matches("ldpi")){
				if (this.density.equals(PixelDensity.ldpi))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("mdpi")){
				if (this.density.equals(PixelDensity.mdpi))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("hdpi")){
				if (this.density.equals(PixelDensity.hdpi))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("xhdpi")){
				if (this.density.equals(PixelDensity.xhdpi))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("nodpi")){
				ret = true;
			}
			else if (tag.getName().matches("tvdpi")){
				if (this.density.equals(PixelDensity.tvdpi))
					ret = true;
				else
					return false;
			}
			else if (tag.getName().matches("notouch")){
				if (this.touch)
					return false;
				else
					ret = true;
			}
			else if (tag.getName().matches("finger")){
				if (!this.touch)
					return false;
				else
					ret = true;
			}
			else if (tag.getName().matches("keysexposed")){
				if (this.querty || this.key12){
					ret = true;
				}else{
					return false;
				}
			}else if (tag.getName().matches("keyshidden")){
				if (this.querty || this.key12){
					ret = true;
				}else{
					return false;
				}
			}else if (tag.getName().matches("nokeys")){
				if (this.querty || this.key12){
					return false;
				}else{
					ret = true;
				}
			}				
			else if (tag.getName().matches("qwerty")){
				if (this.querty){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("12key")){
				if (this.key12){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("navexposed")){
				if (this.dpad || this.track || this.whell){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("navhidden")){
				if (this.dpad || this.track || this.whell){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("nonav")){
				if (!this.dpad && !this.track && !this.whell){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("dpad")){
				if (this.dpad){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("trackball")){
				if (this.track){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("whell")){
				if (this.whell){
					ret = true;
				}else{
					return false;
				}
			}
			else if (tag.getName().matches("v[0-9]+")){
				if (this.versionapi<tag.getSubindex())
					return false;
				else
					ret = true;
			}
					
		}
		return ret;

	}
	
	@Override
	public String toString() {
		return  "Resolution:"+width+"x"+height+"\n"+
				"Dpi:"+dpi+"\n"+
				"smallestWidth:"+sw+"dp\n"+
				"Available width/height:"+wdp+"dp / "+hdp+"dp\n"+
				"Screen size:"+size.toString()+"\n"+
				"Screen aspect:"+aspect.toString()+"\n"+
				"Screen pixel density:"+density.toString()+"\n"+
				"Platform Version (API level): v"+versionapi+"\n"+
				"Touch:"+touch+"\n"+
				"Querty:"+querty+"\n"+
				"12-key keyboard:"+key12+"\n"+
				"D-Pad:"+dpad+"\n"+
				"Trackball:"+track+"\n";
	}

}
