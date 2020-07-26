package com.zhku.mh.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.zhku.mh.gmall.bean.UmsMember;
import com.zhku.mh.gmall.service.UserService;
import com.zhku.mh.gmall.web.annotations.LoginRequired;
import com.zhku.mh.gmall.web.util.CookieUtil;
import com.zhku.mh.gmall.web.util.JwtUtil;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName：
 * Time：2020/7/12 22:09
 * Description：
 * Author： mh
 */
@Controller
public class PassportController {

    @Reference
    private UserService userService;

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {

        String token;

        // 调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);

        if (umsMemberLogin != null) {
            // 登录成功


            // 用jwt制造token
            String userId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", userId);
            userMap.put("nickname", nickname);

            // 通过nginx转发的客户端ip
            String ip = CookieUtil.getIp(request);
            token = JwtUtil.encode("gmall", userMap, ip);

            // 将token存入redis
            userService.addUserToken(token,userId);

        } else {
            // 登录失败
            token = "fail";
        }

        return token;
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token,HttpServletRequest request,String currentIp) {
        // 通过jwt校验token真假
        Map<String,Object> map = new HashMap<>();

        Map<String, Object> decodeMap = JwtUtil.decode(token, "gmall", currentIp);

        if (decodeMap != null) {
            map.put("status","success");
            map.put("memberId",decodeMap.get("memberId"));
            map.put("nickname",decodeMap.get("nickname"));
        }else {
            map.put("status","fail");
        }

        return JSON.toJSONString(map);
    }

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = false)
    public String index(String ReturnUrl, ModelMap modelMap) {

        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";
    }
}
