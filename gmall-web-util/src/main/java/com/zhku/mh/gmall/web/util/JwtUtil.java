package com.zhku.mh.gmall.web.util;

import io.jsonwebtoken.*;

import java.util.Map;

/**
 * ClassName：
 * Time：2020/7/13 20:09
 * Description：
 * Author： mh
 */
public class JwtUtil {

    /**
     * @Description: 加密算法
     * @Param key 服务器秘钥
     * @Param param 用户信息
     * @Param salt 盐值
     * @Return: java.lang.String token 用户标识
     * @Date: 2020/7/13 20:17
     */
    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null) {
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }


    public static Map<String, Object> decode(String token, String key, String salt) {
        Claims claims = null;
        if (salt != null) {
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
        return claims;
    }
}
