package com.sise.controller;

import com.sise.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

//邮箱验证码
@RestController
@RequestMapping("/api")
public class VerificationController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/sendCode")
    public String sendCode(@RequestParam String email) {
        // 生成验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        emailService.sendHtmlMail(email, "【邮箱验证码】欢迎使用xxx系统", "<p>您的邮箱验证码是:<p><p style=\" font-weight: bold;text-align: center;color: red;\">"+code+"</p>" );
        // 存储验证码到 Redis，设置过期时间为 5 分钟
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        //每次发送删除redis对应email的数据
        redisTemplate.delete(email);
        ops.set(email, code, 5, TimeUnit.MINUTES);
        return "验证码已发送";
    }

    @PostMapping("/verifyCode")
    public String verifyCode(@RequestParam String email, @RequestParam String code) {
        // 从 Redis 获取验证码
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String storedCode = ops.get(email);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete(email);
            return "邮箱验证成功";
        } else {
            return "验证码错误或者已失效";
        }
    }
}