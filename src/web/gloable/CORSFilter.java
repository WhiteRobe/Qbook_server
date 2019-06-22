package web.gloable;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(
        filterName = "CORSFilter",
        urlPatterns = "/api/*"
)
public class CORSFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        System.out.println("跨域访问过滤器");
        ((HttpServletResponse)resp).setHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse)resp).setHeader("Access-Control-Allow-Methods", "POST, GET");
        ((HttpServletResponse)resp).setHeader("Access-Control-Max-Age", "999999");
        ((HttpServletResponse)resp).setHeader(
                "Access-Control-Allow-Headers",
                "x-requested-with");
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
