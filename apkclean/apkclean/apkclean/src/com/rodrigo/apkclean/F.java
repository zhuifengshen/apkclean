package com.rodrigo.apkclean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;


public class F {
	public static final String[] States = {
		"mcc[0-9][0-9][0-9]",
		"mnc[0-9][0-9][0-9]",
		"[a-z][a-z]",
		"[a-z][a-z]-r[A-Z][A-Z]|[a-z][a-z]-[A-Z][A-Z]",//pt-rBR or pt-BR
		"port",
		"land",
		"car",
		"desk",
		"television",
		"appliance",
		"night",
		"notnight",
		"keyssoft"
	};
	
	//Padrao do nome e regex para limpar
	public static final String[][] Properties = {
		{"sw[0-9]+dp","^sw|dp$"},
		{"w[0-9]+dp","^w|dp$"},
		{"h[0-9]+dp","^h|dp$"},
		{"small",""},
		{"normal",""},
		{"large",""},
		{"xlarge",""},
		{"long",""} ,
		{"notlong",""},
		{"[0-9]+dpi","^|dpi$"},
		{"ldpi",""} ,
		{"mdpi",""} ,
		{"hdpi",""} ,
		{"xhdpi",""} ,
		{"nodpi",""} ,
		{"tvdpi",""} ,
		{"notouch",""}, 
		{"finger",""} ,
		{"keysexposed",""}, 
		{"keyshidden",""}, 
		{"nokeys",""} ,
		{"qwerty",""} ,
		{"12key",""} ,
		{"navexposed",""}, 
		{"navhidden",""} ,
		{"nonav",""} ,
		{"dpad",""} ,
		{"trackball",""}, 
		{"whell",""} ,
		{"v[0-9]+","^v"}
	};
	
	public static int IsProperty(String tag){
		for (int i=0;i<F.Properties.length;i++){
			if(tag.matches(F.Properties[i][0]))
				return i;
		}
		
		return -1;
	}
	
	public static String FormatMB(long size){
		final float K=1024*1024;
		final float M=K*1024;
		
		if(size<0)
			return "undefined";
		else if(size>=0 && size<1024)
			return size+"B";
		else if(size>=1024 && size<K)
			return String.format("%.1fKB", size/1024f);
		else if(size>=K && size<M)
			return String.format("%.1fMB", size/K);
		else 
			return String.format("%.1fGB", size/M);
	}
	
	public static void Exec(String[] command, File workdir, boolean print){
		try {
			String exename=command[0];
			String os = System.getProperty("os.name").toLowerCase();
			
			if (os.indexOf("win")>=0){
				exename+=".exe";
				for (int i=0;i<command.length;i++)
					command[i].replace("/", "\\");
			}else if (os.indexOf("nux")>=0){
				//exename=("./"+exename);
				for (int i=0;i<command.length;i++)
					command[i].replace("\\", "/");
			}
			//  else if (os.indexOf("mac")>=0)*/
			
						
			String curDir = System.getProperty("user.dir").replace("\\", "/").replaceAll("/$", "");
			
			//System.out.println(os+"  "+curDir+"/"+exename+" "+param+" "+workdir);
			
			command[0]=curDir+"/"+exename;
			
			Process pr = Runtime.getRuntime().exec(command,null,workdir);
			
			InputStream input = pr.getErrorStream();
			 
			int c;
	        String log = "";
	        while((c=input.read()) != -1) {
	        	if (print)
	        		System.out.print((char) c);
	        	log+=((char) c);
	        }
	        input.close();
            
	        pr.waitFor();
	        if (pr.exitValue()!=0){
				if (print)
					log = "";
	        	throw new InvalidParameterException(log+"\nExit value:"+pr.exitValue()+"\n"+Arrays.toString(command)+"\n"+workdir);
	        }
			
		} catch (Exception e) {
			throw new InvalidParameterException(e.toString()+"\n"+Arrays.toString(command)+"\n"+workdir);
		}
	}
	
	public static void copy(File src, File dst, boolean delete) {
		try {
			InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);

		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    	    
		    in.close();
		    out.close();
		    
		    if (delete) src.delete();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	}
}
