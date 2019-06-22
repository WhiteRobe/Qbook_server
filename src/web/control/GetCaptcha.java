package web.control;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
/**
 * 获取验证码
 */
@WebServlet(
        name = "GetCaptcha",
        urlPatterns = "/GetCaptcha"
)
public final class GetCaptcha extends HttpServlet{
	private String ver[]=new String[62];
	private int imgWeight = 144,imgHeight = 33 , dis = 33;
	//生成一个存了大小写字母和数字0~9的数组，用于生成验证码图片
	public void init(){
		for(int i=0;i<10;i++){
			ver[i]= Integer.toString(i);
		}//0~9
		for(int i=0;i<26;i++){
			ver[i+10]= Character.toString((char) (65 + i));
		}//A~Z
		for(int i=0;i<26;i++){
			ver[i+36]= Character.toString((char) (97 + i));
		}//a~z
	}
	public void doGet(HttpServletRequest req,HttpServletResponse res){
		try{
			res.setContentType("image/jpeg");//定向输出流
			OutputStream os=res.getOutputStream();
			BufferedImage image=new BufferedImage(imgWeight,imgHeight,BufferedImage.TYPE_INT_RGB);
			Graphics g= image.getGraphics();

			g.setColor(new Color(200,200,200));//灰底
			g.fillRect(0,0,imgWeight,imgHeight);

			//画干扰线
			g.setColor(new Color(150,150,150));
			for(int i=0;i<30;i++){
				int x1=(int)(Math.random()*imgWeight);
				int y1=(int)(Math.random()*imgHeight);
				int x2=(int)(Math.random()*imgWeight);
				int y2=(int)(Math.random()*imgHeight);
                //g.setColor(new Color((int)(Math.random()*150),(int)(Math.random()*150),(int)(Math.random()*150)));
				g.drawLine(x1,y1,x2,y2);
			}
			//画验证码
			StringBuilder vali= new StringBuilder();
			for(int i=0;i<4;i++){
				String v=ver[(int)(Math.random()*62)];//*36?
				vali.append(v);
				g.setColor(new Color((int)(Math.random()*150),(int)(Math.random()*150),(int)(Math.random()*150)));
				g.setFont(new Font("Times New Roman",Font.BOLD,25));
				g.drawString(v,dis*i+20,23);
			}

			g.dispose();

			ImageIO.write(image,"JPEG",os);
			//System.out.println("当前验证码:"+vali);
			//Session-feedback
			HttpSession session=req.getSession();
			session.setAttribute("Valicode", vali.toString());

		}catch(Exception e){
			//Todo...
			e.printStackTrace();
		}
	}
}
