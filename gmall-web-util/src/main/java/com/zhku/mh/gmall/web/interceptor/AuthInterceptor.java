package com.zhku.mh.gmall.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.zhku.mh.common.util.HttpClientUtil;
import com.zhku.mh.gmall.web.annotations.LoginRequired;
import com.zhku.mh.gmall.web.util.CookieUtil;
import org.eclipse.jetty.util.StringUtil;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/7/12 21:59
 * Description：
 *
 * @author： mh
 */
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截代码

        //判断拦截的请求的访问的方法的注解（是否是需要拦截的）
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation == null) {
            System.out.println("不需要拦截");
            return true;
        }

        String token = "";
        System.out.println("=====需要登录成功才能使用=====");
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

        if (StringUtil.isNotBlank(oldToken)) {
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if (StringUtil.isNotBlank(newToken)) {
            token = newToken;
        }


        //获得该请求是否必须登录
        boolean b = methodAnnotation.loginSuccess();

        // 调用认证中心进行认证
        String success = "fail";
        Map successMap = new HashMap<>();

        if (StringUtil.isNotBlank(token)) {
            String ip = CookieUtil.getIp(request);

            String successJson = HttpClientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&currentIp=" + ip);

            successMap = JSON.parseObject(successJson, Map.class);

            if (successMap != null) {
                success = String.valueOf(successMap.get("status"));
            }

        }

        if (b) {
            // 必须登录成功才能用

            if (!HttpClientUtil.SUCCESS.equals(success)) {
                // 重定向passport登录
                StringBuffer requestUrl = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl=" + requestUrl);
                return false;
            }
            // 需要将token携带的用户信息写入
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));

            // 验证通过，覆盖cookie的token
            if (StringUtils.isNotBlank(token)) {
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }


        } else {
            // 没有登录也能用，但是必须验证
            if (HttpClientUtil.SUCCESS.equals(success)) {
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));

                if (StringUtils.isNotBlank(token) || "".equals(token)) {
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }

            }
        }

        return true;
    }
}
