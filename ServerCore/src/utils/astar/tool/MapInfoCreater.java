package utils.astar.tool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class MapInfoCreater {
	public String readImage(String path, int step){
		File file = new File(path);
		try {
			BufferedImage bi = ImageIO.read(file);
			int width = bi.getWidth();
			int height = bi.getHeight();
			String info = "";
			for(int y = 0; y < height; y += step){
				for(int x = 0; x < width; x += step){
					int pixel = bi.getRGB(x, y); // 取alpha值，所以必须是png图片
					int alpha = (pixel & 0xFF000000) >>> 24; // 注意这个>>>
					if(alpha > 0){
						info += "1";
					}else{
						info += "0";
					}
				}
				info += "\n";
			}
			return info;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void main(String[] args){
		if(args.length < 2)return;
		String path = args[0];
		int step = Integer.parseInt(args[1]);
		String info = new MapInfoCreater().readImage(path, step);
		System.out.println(info);
		OutputStream os;
		try {
			os = new FileOutputStream(path.substring(0, path.lastIndexOf(".")) + ".map");
			os.write(info.getBytes());
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
