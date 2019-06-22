package web.tool;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public final class FileReciver {
    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 5; // 5MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 10; // 10MB

    public JSONObject accept(HttpServletRequest request, String uploadPath) throws Exception {
        // 检测是否为多媒体上传
        JSONObject returnMsg = new JSONObject();
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new ServletException("文件上传错误-不是多媒体文件");
        } else if(!this.dirCheck(uploadPath)){
            throw new ServletException("文件上传目录不存在");
        }

        ServletFileUpload upload = this.initUpload();

        // 解析请求的内容提取文件数据
        @SuppressWarnings("unchecked")
        List<FileItem> formItems = upload.parseRequest(request);
        if (formItems != null && formItems.size() > 0) {
            // 迭代表单数据
            for (FileItem item : formItems) {
                // 处理不在表单中的字段
                if (!item.isFormField()) {
                    String fileName = new File(item.getName()).getName();
                    String filePath = uploadPath + File.separator + MD5.getMD5(fileName);
                    File storeFile = new File(filePath);
                    // 在控制台输出文件的上传路径
                    System.out.println("文件保存到:" + filePath);
                    // 保存文件到硬盘
                    item.write(storeFile);//直接写出文件
                }
            }
            returnMsg.put("FLAG",true);
            returnMsg.put("MSG","文件上传成功!");
        } else {
            returnMsg.put("FLAG",false);
            returnMsg.put("MSG","没有检测到上传文件");
        }
        return returnMsg;
    }

    public List<FileItem> accept(HttpServletRequest request) throws FileUploadException, ServletException {
        // 检测是否为多媒体上传
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new ServletException("文件上传错误-不是多媒体文件");
        }

        ServletFileUpload upload = this.initUpload();

        // 如果目录不存在则创建

        // 解析请求的内容提取文件数据
        @SuppressWarnings("unchecked")
        List<FileItem> formItems = upload.parseRequest(request);
        return formItems;
    }

    private ServletFileUpload initUpload(int thresholdSize, int maxFileSize, int maxRequestSize, String encoding){
        // 配置上传参数
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
        factory.setSizeThreshold(thresholdSize);
        // 设置临时存储目录
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
        ServletFileUpload upload = new ServletFileUpload(factory);
        // 设置最大文件上传值
        upload.setFileSizeMax(maxFileSize);
        // 设置最大请求值 (包含文件和表单数据)
        upload.setSizeMax(maxRequestSize);
        // 中文处理
        upload.setHeaderEncoding(encoding);
        return upload;
    }

    private ServletFileUpload initUpload(){
        return this.initUpload(MEMORY_THRESHOLD, MAX_FILE_SIZE, MAX_REQUEST_SIZE,"UTF-8" );
    }

    private boolean dirCheck(String url){
        File uploadDir = new File(url);
        if (!uploadDir.exists()) {
            return uploadDir.mkdir();
        }
        return true;
    }
}
