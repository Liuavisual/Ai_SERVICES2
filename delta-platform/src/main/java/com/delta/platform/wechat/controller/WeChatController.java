package com.delta.platform.wechat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.Arrays;

@io.swagger.v3.oas.annotations.tags.Tag(name = "微信公众号接入", description = "微信公众号接入接口")
/**
 * 微信公众号控制器，处理微信消息回调和验证
 *
 * @author delta
 */
@RestController
@RequestMapping("/wechat")
@ConditionalOnProperty(prefix = "wx.mp", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WeChatController {

    private static final Logger log = LoggerFactory.getLogger(WeChatController.class);

    @Value("${wx.mp.token:}")
    private String wechatToken;

    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        log.info("收到微信验证请求: timestamp={}, nonce={}", timestamp, nonce);

        if (!verifySignature(signature, timestamp, nonce)) {
            log.warn("微信签名验证失败: signature={}", signature);
            return "error";
        }

        log.info("微信签名验证成功");
        return echostr != null ? echostr : "error";
    }

    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestParam(name = "signature", required = false) String signature,
                       @RequestParam(name = "timestamp", required = false) String timestamp,
                       @RequestParam(name = "nonce", required = false) String nonce,
                       @RequestBody String requestBody) {
        log.info("收到微信消息请求");

        if (!verifySignature(signature, timestamp, nonce)) {
            log.warn("微信消息签名验证失败");
            return "<xml></xml>";
        }

        log.info("微信消息签名验证成功，处理消息");
        return "<xml></xml>";
    }

    private boolean verifySignature(String signature, String timestamp, String nonce) {
        if (signature == null || timestamp == null || nonce == null) {
            return false;
        }
        if (wechatToken == null || wechatToken.isEmpty()) {
            log.error("微信Token未配置，拒绝所有请求");
            return false;
        }
        try {
            String[] arr = new String[]{wechatToken, timestamp, nonce};
            Arrays.sort(arr);
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString().equals(signature);
        } catch (Exception e) {
            log.error("微信签名验证异常", e);
            return false;
        }
    }
}
