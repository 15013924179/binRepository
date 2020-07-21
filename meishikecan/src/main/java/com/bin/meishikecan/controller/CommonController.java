package com.bin.meishikecan.controller;


import com.alibaba.fastjson.JSONObject;
import com.bin.meishikecan.entity.Account;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CommonController {

    /**
     * 登录方法
     *
     * @return
     */
    @PostMapping(value = "/ajaxLogin")
    public String ajaxLogin(Account account) {
        JSONObject jsonObject = new JSONObject();
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(account.getUsername(), account.getPassword());
        try {
            subject.login(token);
            jsonObject.put("token", subject.getSession().getId());
            jsonObject.put("msg", "登录成功");
        } catch (IncorrectCredentialsException e) {
            jsonObject.put("msg", "密码错误");
        } catch (LockedAccountException e) {
            jsonObject.put("msg", "登录失败，该用户已被冻结");
        } catch (AuthenticationException e) {
            jsonObject.put("msg", "该用户不存在");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static void main(String[] args) {
        String hashAlgorithmName = "MD5";//加密方式
        Object crdentials = "123456";//密码原值
        String salt = "admin123456";//盐值
        int hashIterations = 2;//加密1024次
        Object result = new SimpleHash(hashAlgorithmName,crdentials,salt,hashIterations);
        System.out.println(result);
    }
}
