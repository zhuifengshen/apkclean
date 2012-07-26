package com.rodrigo.apkclean;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;


public class Main {
	private static final String version = "0.9.01";
	private static ResourceGroup resources_group;
		
	private static final String help="\n"+
			"ApkClean "+ version +" - Remove unused resources from apk\n"+
			"Copyright 2012 Rodrigo Corsi <rodrigocorsi@gmail.com>\n"+
			"Reference:http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources\n"+
			"Thank's for apktool (http://code.google.com/p/android-apktool/)\n"+
			"\n"+
			"Usage:\n"+
			"  apkclean -w<width px> -h<height px> -d<dpi> -a<api version> <apk list> \n"+
			"\n"+
			"Options:\n"+
			"  -w <width pixel>  - (Required) Screen width in pixel\n"+
			"  -h <height pixel> - (Required) Screen height in pixel\n"+
			"  -d <screen dpi>   - (Required) Screen dpi (120/160/240/320)\n"+
			"  -a <api version>  - (Required) Android Api version\n"+
			"\nExtra options:\n"+
			"  --if <framework> [<tag>] - Install framework\n"+
			"                             Install framework file to your system\n"+
			"  --out <dir>              - Output directory\n"+
			"  --test                   - Test only\n"+
			"  --noapktool              - No use apktool method use aapt method\n"+
			"                             aapt method produces bigger file than apktool"+
			"  --try                    - Try apktool method first, if have error use aapt method\n"+
			"\nExtra device Information:\n"+
			"  --notouch                - Device does not have a touchscreen\n"+
			"  --qwerty                 - Device has a hardware qwerty keyboard\n"+
			"  --12key                  - Device has a hardware 12-key keyboard\n"+
			"  --dpad                   - Device has a directional-pad for nav.\n"+
			"  --trackball              - Device has a trackball for navigation\n"+
			"  --whell                  - Device has a directional wheel for nav.\n";
	
	private static final String[] uncompress_res=
		{"jpg", "jpeg", "png", "gif",
		 "wav", "mp2", "mp3", "ogg", "aac",
		 "mpg", "mpeg", "mid", "midi", "smf", "jet",
		 "rtttl", "imy", "xmf", "mp4", "m4a",
		 "m4v", "3gp", "3gpp", "3g2", "3gpp2",
		 "amr", "awb", "wma", "wmv", "arsc"}; //resources.arsc
	
	public static void main(String args[]) {
		DeviceInfo device = new DeviceInfo();
		ArrayList<File> apklist=new ArrayList<File>();
		File dirout=null;
		boolean test=false;
		int required=0;
		String framework=null;
		String frameworktag=null;
		boolean noapktool = false;
		boolean tryfirst = false;
		
		for (int i=0;i<args.length;i++){
			String p=args[i];
			
			if (p.startsWith("-w")){
				required |= 2;
				if (p.equals("-w")){
					i++;
					device.setWidth(TryInt(args[i]));
				}else{
					device.setWidth(TryInt(p.substring(2)));
				}
			}else if (p.startsWith("-h")){
				required |= 4;
				if (p.equals("-h")){
					i++;
					device.setHeight(TryInt(args[i]));
				}else{
					device.setHeight(TryInt(p.substring(2)));
				}
			}else if (p.startsWith("-d")){
				required |= 8;
				if (p.equals("-d")){
					i++;
					device.setDpi(TryInt(args[i]));
				}else{
					device.setDpi(TryInt(p.substring(2)));
				}
			}else if (p.startsWith("-a")){
				required |= 16;
				if (p.equals("-a")){
					i++;
					device.setVersionapi(TryInt(args[i]));
				}else{
					device.setVersionapi(TryInt(p.substring(2)));
				}
			}else if (!p.startsWith("-")){
				apklist.add(new File(p));
			}else if (p.equals("--if")){
				i++;
				framework=args[i];
				if (args.length>(i+1)){
					if (!args[i+1].startsWith("-")){
						i++;
						frameworktag=args[i];
					}
				}
			}else if (p.equals("--out")){
				i++;
				dirout=new File(args[i]);
			}else if (p.equals("--test")){
				test=true;
			}else if (p.equals("--try")){
				tryfirst=true;
			}else if (p.equals("--noapktool")){
				noapktool=true;
			}else if (p.equals("--notouch")){
				device.touch = false;
			}else if (p.equals("--qwerty")){
				device.querty = true;
			}else if (p.equals("--12key")){
				device.key12 = true;
			}else if (p.equals("--dpad")){
				device.dpad = true;
			}else if (p.equals("--trackball")){
				device.track = true;
			}else if (p.equals("--whell")){
				device.whell = true;
			}else {
				System.out.println("ERROR");
				System.out.println("Unrecognized:"+p);
				System.out.println("\n"+help);
				System.exit(1);
			}
		}
		
		if (required<30){
			System.out.println("Fill all REQUIRED parameters ");
			System.out.println(help);
			System.exit(1);
		}
		
		if (apklist.size()==0){
			System.out.println("Filename not found");
			System.exit(1);
		}
				
		//Check if exist output dir and make it
		if (dirout!=null){
			if (dirout.isFile()){
				System.out.println("Output directory is a File");
				System.exit(1);
			}
			
			if (!dirout.exists())
				dirout.mkdir();
		}
		
		//Install framework
		if (framework!=null){
			System.out.println("Instaling framework: "+framework);
			InstallFramework(framework, frameworktag);
		}
		
		//Print device information
		System.out.println(device);
		
		
		for (File apkin:apklist){
			File extractdir=null;
			
			if (dirout==null)
				extractdir = apkin.getParentFile();
			else
				extractdir = dirout;
			
			//remove extension
			String apkname = apkin.getName().replaceAll("\\.[A-Za-z0-9]+$", "");
			
			//check if exist dir to extract apk
			File dir = new File(extractdir, apkname);
			int cont = 0;
			String tmpdir = apkname;
			
			while (dir.exists()){
				tmpdir= apkname+(++cont);
				dir = new File(extractdir, tmpdir);
			}
			
			File tmpapk = new File(extractdir, tmpdir+"_tmp.apk");
						
			File apkout = null;
			if(apkin.getParentFile().equals(extractdir)){
				apkout = new File(extractdir, tmpdir+"_new.apk");
			}else{
				apkout = new File(extractdir, tmpdir+".apk");
			}
			
			boolean ret = false;
			
			if (noapktool){
				System.out.println("AAPT method");
				ret = startCleanApkAAPT(device,dir,apkin,tmpapk,apkout, test);
			}else if(tryfirst){
				System.out.println("Try apktool method");
				ret = startCleanApkApktool(device,dir,apkin,tmpapk,apkout, test); 
				if(!ret){
					System.out.println("try AAPT method");
					ret=startCleanApkAAPT(device,dir,apkin,tmpapk,apkout, test);
				}
			}else{
				System.out.println("Apktool method");
				ret=startCleanApkApktool(device,dir,apkin,tmpapk,apkout, test);
			}
			
			if(ret)
				System.out.println("Complete new file create:"+apkout.getName());
			else
				System.out.println("Error on clean "+apkin.getName());
		}
				
	}
	
	
	/**
	 * 
	 * @param device
	 * @param dir - location for descompression apk
	 * @param apk - apkname
	 * @param tmpapk - name of rebuild apk  
	 * @param newapk - new apk name after the process
	 * @param test
	 */
	private static boolean startCleanApkAAPT(DeviceInfo device, File dir, File apk, File tmpapk, File newapk, boolean test){
		resources_group = new ResourceGroup(apk, device, new String[]{"drawable","mipmap","raw"}) ;
		
		long size = resources_group.SumRemoveableRes();
		System.out.println("Total size removed:"+F.FormatMB(size));
		
		if(test)
			printResources(dir,false);
		
		if (size>0 && !test){
			//process aapt
			if(!dir.exists())
				dir.mkdirs();
			
			printResources(dir,true);
			
			File buildapknew = new File(dir,"new.apk");
			
			F.copy(apk,buildapknew,false);
			
			CleanAAPT(buildapknew);
			
			F.copy(buildapknew, tmpapk, true);
			
			ZipAlign(tmpapk, newapk);
			
			DeleteFile(tmpapk);
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param device
	 * @param dir - location for descompression apk
	 * @param apk - apkname
	 * @param tmpapk - name of rebuild apk  
	 * @param newapk - new apk name after the process
	 * @param test
	 */
	private static boolean startCleanApkApktool(DeviceInfo device, File dir, File apk, File tmpapk, File newapk, boolean test){
		
		resources_group = new ResourceGroup(apk, device, new String[]{"drawable","mipmap","raw"}) ;
		
		long size = resources_group.SumRemoveableRes();
		System.out.println("Total size removed:"+F.FormatMB(size));
		
		if(test)
			printResources(dir,false);
				
		//process apktool
		if (size>0 && !test){
			if(!Descompress(apk, dir))
				return false;
			printResources(dir,true);
			
			CleanApktool(dir);
						
			if(!Compress(dir))
				return false;
			
			File buildapknew = new File(dir,"/build/apk/new.apk");
			
			F.copy(apk, buildapknew , false);
			
			BuildApk(dir, buildapknew.getParentFile(), buildapknew);
			
			F.copy(buildapknew, tmpapk, false);
					
			ZipAlign(tmpapk, newapk);
			
			DeleteFile(tmpapk);
			
		}else{
			F.copy(apk,newapk,false);
		}
		
		//DeleteFile(dir);
		return true;
	}
	
	
	private static void CleanAAPT(File apk){
		for(String key: resources_group.keySet()){
			ArrayList<Resource> res_list = resources_group.get(key);
			for (Resource res: res_list){
				if (!res.used){
					ExtractZip(apk, res.file.getPath(), apk.getParentFile());
					File e = new File(apk.getParentFile(),res.file.getPath());
					EmptyFile(e);
					
					ApkRemove(apk, res.file.getPath(), apk.getParentFile());
					ApkStore(apk, e, res.file.getPath(), apk.getParentFile());

				}
			}
		}
	}
		
	
	private static void CleanApktool(File dir){
		for(String key: resources_group.keySet()){
			ArrayList<Resource> res_list = resources_group.get(key);
			for (Resource res: res_list){
				if (!res.used){
					
					File e = new File(dir,convFileinfolder(res.file.getPath()));
					if(res_list.size()==1){
						EmptyFile(e);
					}else{
						DeleteFile(e);
					}

				}
			}
		}
	}
	
	private static void ApkRemove(File apk, String file, File workdir){
		file = file.replace("\\", "/").replaceAll("^/", "");

		F.Exec(new String[]{
				"aapt",
				"r", 
				apk.getAbsolutePath(), 
				file
				},workdir, false);
	}
	
	private static void ApkStore(File apk, File file_ori, String file_dest, File workdir){
		boolean create = false;
		//Create destiny
		File dest = new File(apk.getParentFile(),file_dest);
		if (!dest.exists()){
			F.copy(file_ori, dest, false);
			create = true;
		}
		
		file_dest = file_dest.replace("\\", "/").replaceAll("^/", "");
		String[] tmp = file_dest.split("\\.");
		String extension = tmp[tmp.length-1].toLowerCase();
		boolean compress = true;
		
		//Check if this extension is compressed
		for(String ext:uncompress_res){
			if (ext.equals(extension)){
				compress = false;
				break;
			}
		}
		
		if (!compress){
			F.Exec(new String[]{
					"aapt", "a", "-0", "\"\"",
					apk.getAbsolutePath(), 
					file_dest},workdir, false);
		}else{
			F.Exec(new String[]{
					"aapt",	"a",
					apk.getAbsolutePath(), 
					file_dest },workdir, false);
		}
		
		if (create)
			dest.delete();
	}
	
	private static void ZipAlign(File tmpapk, File newapk){
		F.Exec(new String[]{
				"zipalign",
				"-v",
				"4",
				tmpapk.getAbsolutePath(),
				newapk.getAbsolutePath()
				},null, false);
	}
	
	
	private static void BuildApk(File dir, File workdir, File apk){
		File fwdir = workdir;
		
		for (String key: resources_group.keySet()){
			ArrayList<Resource> res_list = resources_group.get(key);
			for (Resource res: res_list){
				if (!res.used){
					String fileinzip = res.file.getPath();
					String fileinfolder = convFileinfolder(fileinzip);
					
					if (res_list.size()==1){//Empty
						ApkRemove(apk, fileinzip, fwdir);
						ApkStore(apk,new File(apk.getParentFile(),fileinfolder), fileinzip, fwdir);
					}else{//Delete
						ApkRemove(apk, fileinzip, fwdir);
					}
				}
			}
		}
				
		ApkRemove(apk, "resources.arsc", fwdir);
		ApkStore(apk, new File(apk.getParentFile(),"resources.arsc"),"resources.arsc", fwdir);
	}
	
	private static boolean DeleteFile(File f) {
	    
	    // Make sure the file or directory exists and isn't write protected
	    if (!f.exists())
	      throw new IllegalArgumentException(
	          "Delete: no such file or directory: " + f.getAbsolutePath());

	    if (!f.canWrite())
	      throw new IllegalArgumentException("Delete: write protected: " + f.getAbsolutePath());

	    boolean success=false;
	    if (f.isDirectory()){
	    	try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }else{
	    	success= f.delete();
	    }
	    
	    if (!success)
	      throw new IllegalArgumentException("Delete: deletion failed "+f.getAbsolutePath());
	    
	    return true;
	}

	private static boolean EmptyFile(File name){

		try {
			File f = name;
			String filename = f.getName();
			String[] tmp = filename.split("\\.");
			String extension = tmp[tmp.length-1].toLowerCase();
			
			if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif") && !extension.equals("jpeg"))
				return false;
			
			if (extension.equals("png") && tmp[tmp.length-2].equals("9"))//TODO Empty 9.png
				return false;
			
			//ImageInfo
			ImageInfo info = new ImageInfo();
			FileInputStream in = new FileInputStream (f);
			info.setInput(in);
			
			if (!info.check()) {
				System.out.println("Not a supported image file format\n"+name.getAbsolutePath());
				return false;
			}
						
			int w =info.getWidth();
			int h =info.getHeight();
			
			in.close();
			
			if (!DeleteFile(f))
				return false;
			
		    BufferedImage newimg = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_INDEXED);
		    
		    ImageIO.write(newimg, extension, f);
		    
		    
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Filename:"+name.getAbsolutePath());
			return false;
		}
		
		
		
	    return true;
	}
	
	private static boolean Descompress(File apk, File dir){
		ApkDecoder decoder = new ApkDecoder();
		File outDir = dir;
		try {
			decoder.setOutDir(outDir);
			decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_NONE); //no decode source code
		} catch (AndrolibException e) {
			System.out.println("Error during decompression");
			e.printStackTrace();
			return false;
		}
		decoder.setApkFile(apk);

		try {
			decoder.decode();
			System.out.println("Apk Descompress");
		} catch (AndrolibException e) {
			System.out.println("Error during decompression");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static boolean Compress(File dir){
		try {

			new Androlib().build(dir,
					null, 
					false,// Brute_force
					false);// Debug
			
		} catch (AndrolibException e) {
			System.out.println("Error during rebuild");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static void InstallFramework(String framework, String tag){
		try {
        	new Androlib().installFramework(new File(framework), tag);
            return;
		} catch (Exception e) {
			System.out.println("Error during Install framework");
			e.printStackTrace();
			System.exit(1);
		}
                
    }
		
	private static int TryInt(String value){
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			System.out.println("Not recognized as integer:"+value);
			System.exit(-1);
			return 0;
		}
	}
	
	private static void printResources(File dir, boolean infile){
		String log ="";
		long total = 0;
		long removed = 0;
		
		for(String key: resources_group.keySet()){
			ArrayList<Resource> res_list = resources_group.get(key);
			log+= (key+"\n ");
			for (Resource res: res_list){
				String del = "    ";
				if (!res.used && res_list.size()==1)
					del = "EMP ";
				else if(!res.used)
					del = "DEL ";
				
				String fileinzip = res.file.getPath();
				
				log+=("\t"+del+fileinzip+" "+F.FormatMB(res.size)+"\n");
				total+=res.size;
				if(!res.used) removed+=res.size;
			}
		}
		log+=("TOTAL:"+F.FormatMB(total)+" WILL BE REMOVED:"+F.FormatMB(removed));
		
		if(!infile){
			System.out.println(log);
			return ;
		}
		
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(new File(dir, "log.txt")));
			out.write(log);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void ExtractZip(File zip, String filename, File diroutput) {
		byte[] buf = new byte[1024];
		
		filename = filename.replace("\\", "/").replaceAll("^/", "");
		
		ZipInputStream zinstream;
		File output = new File(diroutput, filename);
		if(!output.getParentFile().exists())
			output.getParentFile().mkdirs();
		
		try {
			zinstream = new ZipInputStream(new FileInputStream(zip));
			ZipEntry zentry = zinstream.getNextEntry();
						
			while (zentry != null) {
				String entryName = zentry.getName();
				if (entryName.equals(filename)){
								
					FileOutputStream outstream = new FileOutputStream(output);
					int n;
	
					while ((n = zinstream.read(buf, 0, 1024)) > -1) {
						outstream.write(buf, 0, n);
					}
					
					outstream.close();

				}
				zinstream.closeEntry();
				zentry = zinstream.getNextEntry();
			}
			zinstream.close();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	
	public static String convFileinfolder(String fileinzip){
		String ret = fileinzip.replace("-320dpi", "-xhdpi");
		ret = ret.replace("-240dpi", "-hdpi");
		ret = ret.replace("-213dpi", "-tvdpi");
		ret = ret.replace("-160dpi", "-mdpi");
		ret = ret.replace("-120dpi", "-ldpi");
		ret = ret.replaceAll("-r(?=[A-Z][A-Z])", "-");//replace en-rUK to en-UK 
		return ret;
	}
	
}
