package web.gloable;

import javax.servlet.*;
import java.io.IOException;

//@WebFilter(filterName = "EncodingFilter")
public class EncodingFilter implements Filter {
    private static String encoding = "UTF-8";
    public void destroy() {

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        //HttpServletRequest request = (HttpServletRequest) req;
        req.setCharacterEncoding(encoding); // 改过滤对GET不起效
        //resp.setCharacterEncoding(encoding);
        resp.setContentType("text/html;charset=UTF-8");
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        encoding = config.getInitParameter("Encoding");
    }

}
