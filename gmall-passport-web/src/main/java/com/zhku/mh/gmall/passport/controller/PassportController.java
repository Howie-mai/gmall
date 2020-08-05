package com.zhku.mh.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.bind.v2.TODO;
import com.zhku.mh.common.util.HttpClientUtil;
import com.zhku.mh.gmall.bean.UmsMember;
import com.zhku.mh.gmall.service.CartService;
import com.zhku.mh.gmall.service.UserService;
import com.zhku.mh.gmall.web.annotations.LoginRequired;
import com.zhku.mh.gmall.web.util.CookieUtil;
import com.zhku.mh.gmall.web.util.JwtUtil;
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

    /**
     * 获取微博access_token的请求地址
     */
    private static final String GET_ACCESS_TOKEN_URL = "https://api.weibo.com/oauth2/access_token";

    /**
     * 获取微博用户信息的请求地址
     */
    private static final String GET_WB_USER_INFO_URL = "https://api.weibo.com/2/users/show.json";

    /**
     * TODO 登录同步cookie购物车数据
     */
    @Reference
    private CartService cartService;

    @Reference
    private UserService userService;

    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){

        // 授权码换取access_token
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","2173054083");
        paramMap.put("client_secret","f043fe09dcab7e9b90cdd7491e282a8f");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://localhosst:8085/vlogin");
        // 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
        paramMap.put("code",code);
        String access_token_json = HttpClientUtil.doPost(GET_ACCESS_TOKEN_URL, paramMap);

        if(StringUtils.isBlank(access_token_json)){
            return "error";
        }

        Map<String,Object> map = JSON.parseObject(access_token_json,Map.class);

        String uid = String.valueOf(map.get("uid"));
        String accessToken = String.valueOf(map.get("access_token"));
        String showInfoUrl = GET_WB_USER_INFO_URL + "?access_token=" + accessToken + "&uid=" + uid;
        String user_json = HttpClientUtil.doGet(showInfoUrl);

        if(StringUtils.isBlank(user_json)){
            return "error";
        }

        Map<String,Object> userInfoMap = JSON.parseObject(user_json,Map.class);

        // 保存数据库
        String sourceId = String.valueOf(userInfoMap.get("idstr"));
        UmsMember member = new UmsMember();
        member.setSourceUid(sourceId);
        member = userService.selectOneByUmsMember(member);
        if(member.getId() == null){
            // 微博来源
            member.setSourceType("2");
            member.setAccessCode(code);
            member.setAccessToken(accessToken);
            member.setSourceUid(sourceId);
            member.setCity(String.valueOf(userInfoMap.get("location")));
            member.setNickname(String.valueOf(userInfoMap.get("screen_name")));
            String g = "2";
            String gender = String.valueOf(userInfoMap.get("gender"));
            if(gender.equals("m")){
                g = "1";
            }else if (gender.equals("f")){
                g = "0";
            }
            member.setGender(g);
            String id = userService.addOuthUser(member);
            if(id != null){
                member.setId(id);
            }else {
                return "error";
            }
        }

        // 生成token
        String token;
        // rpc的主键返回策略失效
        String memberId = member.getId();
        String nickname = member.getNickname();
        Map<String,Object> userMap = new HashMap<>();
        // 是保存数据库后主键返回策略生成的id
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);

        String ip = CookieUtil.getIp(request);

        token = JwtUtil.encode("gmall",userMap,ip);
        userService.addUserToken(token,memberId);

        return "redirect:http://localhost:8083/index?token" + token;
    }

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

            Map<String, Object> userMap = new HashMap<>(4);
            userMap.put("memberId", userId);
            userMap.put("nickname", nickname);

            // 通过nginx转发的客户端ip
            String ip = CookieUtil.getIp(request);
            token = JwtUtil.encode("gmall", userMap, ip);

            // 将token存入redis
            userService.addUserToken(token,userId);

            // TODO 登录同步cookie购物车数据

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
    public String index(ModelMap modelMap,HttpServletRequest request) {

        modelMap.put("returnUrl", request.getRequestURL());
        return "index";
    }


}
