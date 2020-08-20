package com.zhku.mh.gmall.payment.controller;

import com.zhku.mh.gmall.web.annotations.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * ClassName：
 * Time：2020/8/20 21:45
 * Description：
 * Author： mh
 */
@Controller
public class PaymentController {

    @RequestMapping("index")
    @LoginRequired
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, Model model){
        String memberId = String.valueOf(request.getSession().getAttribute("memberId"));
        String nickName = String.valueOf(request.getSession().getAttribute("nickName"));
        if ("null".equals(memberId) || "null".equals(nickName)) {
            return "redirect:http://localhost:8085/index?returnUrl=" + request.getRequestURL();
        }
        model.addAttribute("nickName",nickName);
        model.addAttribute("outTradeNo",outTradeNo);
        model.addAttribute("totalAmount",totalAmount);
        return "index";
    }
}
