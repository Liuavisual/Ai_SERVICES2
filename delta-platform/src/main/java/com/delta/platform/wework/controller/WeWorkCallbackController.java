package com.delta.platform.wework.controller;

import com.delta.common.constant.WeWorkConstants;
import com.delta.common.dto.WeWorkCallbackDTO;
import com.delta.platform.wework.config.WeWorkConfig;
import com.delta.platform.wework.crypto.WeWorkCryptoUtils;
import com.delta.platform.wework.service.WeWorkMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

@Slf4j
@RestController
@RequestMapping("/api/wework")
@ConditionalOnProperty(prefix = "wework", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WeWorkCallbackController {

    private final WeWorkMessageService weWorkMessageService;
    private final WeWorkCryptoUtils weWorkCryptoUtils;

    public WeWorkCallbackController(WeWorkMessageService weWorkMessageService, WeWorkCryptoUtils weWorkCryptoUtils) {
        this.weWorkMessageService = weWorkMessageService;
        this.weWorkCryptoUtils = weWorkCryptoUtils;
    }

    @GetMapping("/callback")
    public String verifyCallback(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {
        log.info("收到企业微信验证请求: timestamp={}, nonce={}", timestamp, nonce);

        try {
            String result = weWorkCryptoUtils.verifySignature(msgSignature, timestamp, nonce, echostr);
            log.info("企业微信验证成功");
            return result;
        } catch (Exception e) {
            log.warn("企业微信验证失败: {}", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/callback")
    public String handleCallback(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String requestBody) {
        log.info("收到企业微信回调消息");

        try {
            String encryptedContent = extractEncryptContent(requestBody);
            if (encryptedContent == null) {
                log.warn("企业微信回调消息体中未找到Encrypt字段");
                return "success";
            }

            String decryptedXml = weWorkCryptoUtils.decryptMessage(msgSignature, timestamp, nonce, encryptedContent);
            log.info("企业微信消息解密成功");

            WeWorkCallbackDTO callbackDTO = parseCallbackMessage(decryptedXml);
            weWorkMessageService.handleCallback(callbackDTO);

        } catch (Exception e) {
            log.error("处理企业微信回调异常: {}", e.getMessage(), e);
        }

        return "success";
    }

    private String extractEncryptContent(String xmlBody) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setNamespaceAware(false);

            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlBody)));
            if (doc.getElementsByTagName("Encrypt").getLength() == 0) {
                return null;
            }
            return doc.getElementsByTagName("Encrypt").item(0).getTextContent();
        } catch (Exception e) {
            log.warn("解析企业微信XML消息体失败: {}", e.getMessage());
            return null;
        }
    }

    private WeWorkCallbackDTO parseCallbackMessage(String xml) {
        WeWorkCallbackDTO dto = new WeWorkCallbackDTO();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setNamespaceAware(false);

            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

            String msgType = getTagValue(doc, "MsgType");
            dto.setMsgType(msgType);

            if (WeWorkConstants.MSG_TYPE_EVENT.equals(msgType)) {
                dto.setEventType(getTagValue(doc, "Event"));
                dto.setExternalUserId(getTagValue(doc, "ExternalUserID"));
                dto.setUserId(getTagValue(doc, "UserID"));
            } else {
                dto.setFromUserName(getTagValue(doc, "FromUserName"));
                dto.setToUserName(getTagValue(doc, "ToUserName"));
                dto.setContent(getTagValue(doc, "Content"));
                dto.setTimestamp(parseLong(getTagValue(doc, "CreateTime")));
            }

        } catch (Exception e) {
            log.error("解析企业微信回调XML失败: {}", e.getMessage(), e);
        }
        return dto;
    }

    private String getTagValue(Document doc, String tagName) {
        try {
            if (doc.getElementsByTagName(tagName).getLength() > 0) {
                return doc.getElementsByTagName(tagName).item(0).getTextContent();
            }
        } catch (Exception e) {
            log.debug("获取XML标签值失败: tagName={}", tagName);
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.debug("时间戳解析失败: {}", value);
            return null;
        }
    }
}
