package com.delta.platform.wework.service.impl;

import com.delta.common.constant.WeWorkConstants;
import com.delta.common.exception.BusinessException;
import com.delta.platform.wework.config.WeWorkConfig;
import com.delta.platform.wework.service.WeWorkApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeWorkApiServiceImpl implements WeWorkApiService {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WeWorkConfig weWorkConfig;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public String getAccessToken(String tokenType) {
        String key = WeWorkConstants.TOKEN_CACHE_PREFIX + tokenType;
        String token = redisTemplate.opsForValue().get(key);
        if (token != null) {
            return token;
        }

        String lockKey = WeWorkConstants.TOKEN_LOCK_PREFIX + tokenType;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(WeWorkConstants.TOKEN_LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            token = redisTemplate.opsForValue().get(key);
            if (token != null) {
                return token;
            }

            token = refreshToken(tokenType);
            if (token == null) {
                throw new BusinessException("获取企业微信access_token返回为空");
            }
            redisTemplate.opsForValue().set(key, token, WeWorkConstants.TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            return token;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void sendTextMessage(String externalUserId, String content) {
        String token = getAccessToken(WeWorkConstants.TOKEN_TYPE_APP);
        String url = WeWorkConstants.API_BASE_URL + WeWorkConstants.SEND_MESSAGE_PATH
                + "?access_token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("touser", externalUserId);
        body.put("msgtype", WeWorkConstants.MSG_TYPE_TEXT);
        body.put("agentid", weWorkConfig.getAgentId());
        body.put("text", Map.of("content", content));

        Map<String, Object> result = postForMap(url, body);
        validateResponse(result, "发送消息");
    }

    @Override
    public void sendWelcomeMessage(String externalUserId, String content) {
        String token = getAccessToken(WeWorkConstants.TOKEN_TYPE_CONTACT);
        String url = WeWorkConstants.API_BASE_URL + WeWorkConstants.SEND_WELCOME_MSG_PATH
                + "?access_token=" + token;

        Map<String, Object> textContent = new HashMap<>();
        textContent.put("content", content);

        Map<String, Object> body = new HashMap<>();
        body.put("external_userid", externalUserId);
        body.put("text", textContent);

        Map<String, Object> result = postForMap(url, body);
        validateResponse(result, "发送欢迎语");
    }

    @Override
    public Map<String, Object> getExternalContact(String externalUserId) {
        String token = getAccessToken(WeWorkConstants.TOKEN_TYPE_CONTACT);
        String url = WeWorkConstants.API_BASE_URL + WeWorkConstants.GET_EXTERNAL_CONTACT_PATH
                + "?access_token=" + token + "&external_userid=" + externalUserId;

        Map<String, Object> result = getForMap(url);
        validateResponse(result, "获取外部联系人");
        return result;
    }

    @Override
    public Map<String, Object> getUserInfo(String userId) {
        String token = getAccessToken(WeWorkConstants.TOKEN_TYPE_APP);
        String url = WeWorkConstants.API_BASE_URL + WeWorkConstants.GET_USER_INFO_PATH
                + "?access_token=" + token + "&userid=" + userId;

        Map<String, Object> result = getForMap(url);
        validateResponse(result, "获取用户信息");
        return result;
    }

    private Map<String, Object> getForMap(String url) {
        URI uri = Objects.requireNonNull(URI.create(url), "URI不能为空");
        HttpMethod method = Objects.requireNonNull(HttpMethod.GET, "HttpMethod.GET不能为空");
        RequestEntity<Void> request = new RequestEntity<>(method, uri);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                request, Objects.requireNonNull(MAP_TYPE, "MAP_TYPE不能为空"));
        Map<String, Object> body = response.getBody();
        return body != null ? body : Map.of();
    }

    private Map<String, Object> postForMap(String url, Map<String, Object> body) {
        URI uri = Objects.requireNonNull(URI.create(url), "URI不能为空");
        HttpMethod method = Objects.requireNonNull(HttpMethod.POST, "HttpMethod.POST不能为空");
        RequestEntity<Map<String, Object>> request = new RequestEntity<>(body, method, uri);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                request, Objects.requireNonNull(MAP_TYPE, "MAP_TYPE不能为空"));
        Map<String, Object> result = response.getBody();
        return result != null ? result : Map.of();
    }

    private String refreshToken(String tokenType) {
        String secret = WeWorkConstants.TOKEN_TYPE_CONTACT.equals(tokenType)
                ? weWorkConfig.getContactSecret()
                : weWorkConfig.getAppSecret();

        String url = WeWorkConstants.API_BASE_URL + WeWorkConstants.TOKEN_PATH
                + "?corpid=" + weWorkConfig.getCorpId()
                + "&corpsecret=" + secret;

        Map<String, Object> result = getForMap(url);
        if (result == null || !WeWorkConstants.ERRCODE_SUCCESS.equals(String.valueOf(result.get("errcode")))) {
            String errmsg = result != null ? String.valueOf(result.get("errmsg")) : "unknown";
            log.error("获取access_token失败: {}", errmsg);
            throw new BusinessException("获取企业微信access_token失败: " + errmsg);
        }

        Object tokenObj = result.get("access_token");
        String token = tokenObj instanceof String ? (String) tokenObj : String.valueOf(tokenObj);
        if (token == null || token.isEmpty()) {
            throw new BusinessException("获取企业微信access_token返回为空");
        }

        log.info("企业微信access_token刷新成功, type={}", tokenType);
        return token;
    }

    private void validateResponse(Map<String, Object> result, String action) {
        if (result == null) {
            throw new BusinessException(action + "失败: 响应为空");
        }
        int errcode = ((Number) result.getOrDefault("errcode", -1)).intValue();
        if (errcode != 0) {
            String errmsg = String.valueOf(result.getOrDefault("errmsg", "unknown"));
            if (errcode == WeWorkConstants.ERRCODE_INVALID_TOKEN
                    || errcode == WeWorkConstants.ERRCODE_EXPIRED_TOKEN) {
                log.warn("{}失败, token无效(errcode={}), 清除缓存重试", action, errcode);
                clearTokenCache(errcode);
                throw new BusinessException(errcode, action + "失败: token无效");
            }
            throw new BusinessException(errcode, action + "失败: " + errmsg);
        }
    }

    private void clearTokenCache(int errcode) {
        if (errcode == WeWorkConstants.ERRCODE_INVALID_TOKEN || errcode == WeWorkConstants.ERRCODE_EXPIRED_TOKEN) {
            redisTemplate.delete(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP);
            redisTemplate.delete(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_CONTACT);
        }
    }
}
