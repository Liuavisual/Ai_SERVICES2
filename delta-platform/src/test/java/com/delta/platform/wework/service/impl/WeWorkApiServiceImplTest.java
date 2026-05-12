package com.delta.platform.wework.service.impl;

import com.delta.common.constant.WeWorkConstants;
import com.delta.common.exception.BusinessException;
import com.delta.platform.wework.config.WeWorkConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 企业微信API服务单元测试
 *
 * @author 刘建国
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("企业微信API服务单元测试")
class WeWorkApiServiceImplTest {

    @InjectMocks
    private WeWorkApiServiceImpl weWorkApiService;

    @Mock
    private WeWorkConfig weWorkConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(weWorkConfig.getCorpId()).thenReturn("test_corp_id");
        when(weWorkConfig.getAppSecret()).thenReturn("test_app_secret");
        when(weWorkConfig.getContactSecret()).thenReturn("test_contact_secret");
        when(weWorkConfig.getAgentId()).thenReturn(1000001);
    }

    @Test
    @DisplayName("基本实例化测试")
    void shouldInstantiate() {
        assertNotNull(weWorkApiService);
    }

    @Test
    @DisplayName("获取access_token - 缓存命中应直接返回")
    void getAccessToken_cacheHit_shouldReturnCachedToken() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("cached_token_123");

        String token = weWorkApiService.getAccessToken(WeWorkConstants.TOKEN_TYPE_APP);

        assertEquals("cached_token_123", token);
        verify(restTemplate, never()).exchange(any(), any(org.springframework.core.ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("获取access_token - 缓存未命中应刷新token并缓存")
    void getAccessToken_cacheMiss_shouldRefreshAndCache() {
        String cacheKey = WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP;
        when(valueOperations.get(cacheKey)).thenReturn(null);

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("errcode", 0);
        tokenResponse.put("errmsg", "ok");
        tokenResponse.put("access_token", "new_token_456");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        String token = weWorkApiService.getAccessToken(WeWorkConstants.TOKEN_TYPE_APP);

        assertEquals("new_token_456", token);
        verify(valueOperations).set(eq(cacheKey), eq("new_token_456"),
                eq(WeWorkConstants.TOKEN_TTL_SECONDS), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("获取access_token - 加锁后二次缓存命中应直接返回")
    void getAccessToken_secondCacheHitAfterLock_shouldReturnToken() {
        String cacheKey = WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP;
        when(valueOperations.get(cacheKey))
                .thenReturn(null)
                .thenReturn("second_cached_token");

        String token = weWorkApiService.getAccessToken(WeWorkConstants.TOKEN_TYPE_APP);

        assertEquals("second_cached_token", token);
        verify(restTemplate, never()).exchange(any(), any(org.springframework.core.ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("获取access_token - API返回错误应抛出BusinessException")
    void getAccessToken_apiError_shouldThrowBusinessException() {
        String cacheKey = WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP;
        when(valueOperations.get(cacheKey)).thenReturn(null);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errcode", 40013);
        errorResponse.put("errmsg", "invalid corpid");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(errorResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        assertThrows(BusinessException.class,
                () -> weWorkApiService.getAccessToken(WeWorkConstants.TOKEN_TYPE_APP));
    }

    @Test
    @DisplayName("获取access_token - contact类型应使用contactSecret")
    void getAccessToken_contactType_shouldUseContactSecret() {
        String cacheKey = WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_CONTACT;
        when(valueOperations.get(cacheKey)).thenReturn(null);

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("errcode", 0);
        tokenResponse.put("errmsg", "ok");
        tokenResponse.put("access_token", "contact_token_789");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        String token = weWorkApiService.getAccessToken(WeWorkConstants.TOKEN_TYPE_CONTACT);

        assertEquals("contact_token_789", token);
        verify(weWorkConfig).getContactSecret();
    }

    @Test
    @DisplayName("发送文本消息 - 成功应无异常")
    void sendTextMessage_success_shouldNotThrow() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("valid_token");

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("errcode", 0);
        successResponse.put("errmsg", "ok");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        assertDoesNotThrow(() -> weWorkApiService.sendTextMessage("external_user_001", "你好"));
    }

    @Test
    @DisplayName("发送文本消息 - API返回错误应抛出BusinessException")
    void sendTextMessage_apiError_shouldThrowBusinessException() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("valid_token");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errcode", 40014);
        errorResponse.put("errmsg", "invalid access_token");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(errorResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weWorkApiService.sendTextMessage("external_user_001", "你好"));
        assertTrue(exception.getMessage().contains("发送消息失败"));
    }

    @Test
    @DisplayName("发送欢迎语 - 成功应无异常")
    void sendWelcomeMessage_success_shouldNotThrow() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_CONTACT))
                .thenReturn("valid_contact_token");

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("errcode", 0);
        successResponse.put("errmsg", "ok");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(successResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        assertDoesNotThrow(() -> weWorkApiService.sendWelcomeMessage("external_user_001", "欢迎关注"));
    }

    @Test
    @DisplayName("获取外部联系人 - 成功应返回联系人信息")
    void getExternalContact_success_shouldReturnContactInfo() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_CONTACT))
                .thenReturn("valid_contact_token");

        Map<String, Object> contactResponse = new HashMap<>();
        contactResponse.put("errcode", 0);
        contactResponse.put("errmsg", "ok");
        contactResponse.put("external_contact", Map.of("external_userid", "ext_001", "name", "测试客户"));
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(contactResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = weWorkApiService.getExternalContact("ext_001");

        assertNotNull(result);
        assertEquals(0, result.get("errcode"));
    }

    @Test
    @DisplayName("获取用户信息 - 成功应返回用户信息")
    void getUserInfo_success_shouldReturnUserInfo() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("valid_app_token");

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("errcode", 0);
        userResponse.put("errmsg", "ok");
        userResponse.put("userid", "user_001");
        userResponse.put("name", "测试员工");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(userResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Map<String, Object> result = weWorkApiService.getUserInfo("user_001");

        assertNotNull(result);
        assertEquals(0, result.get("errcode"));
    }

    @Test
    @DisplayName("token无效时应清除缓存并抛出异常")
    void validateResponse_invalidToken_shouldClearCacheAndThrow() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("invalid_token");

        Map<String, Object> invalidTokenResponse = new HashMap<>();
        invalidTokenResponse.put("errcode", WeWorkConstants.ERRCODE_INVALID_TOKEN);
        invalidTokenResponse.put("errmsg", "invalid access_token");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(invalidTokenResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weWorkApiService.sendTextMessage("ext_001", "测试"));
        assertTrue(exception.getMessage().contains("token无效"));
        verify(redisTemplate).delete(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP);
        verify(redisTemplate).delete(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_CONTACT);
    }

    @Test
    @DisplayName("token过期时应清除缓存并抛出异常")
    void validateResponse_expiredToken_shouldClearCacheAndThrow() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("expired_token");

        Map<String, Object> expiredTokenResponse = new HashMap<>();
        expiredTokenResponse.put("errcode", WeWorkConstants.ERRCODE_EXPIRED_TOKEN);
        expiredTokenResponse.put("errmsg", "access_token expired");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(expiredTokenResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weWorkApiService.sendTextMessage("ext_001", "测试"));
        assertTrue(exception.getMessage().contains("token无效"));
        verify(redisTemplate).delete(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP);
        verify(redisTemplate).delete(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_CONTACT);
    }

    @Test
    @DisplayName("获取access_token - 返回空token应抛出BusinessException")
    void getAccessToken_emptyToken_shouldThrowBusinessException() {
        String cacheKey = WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP;
        when(valueOperations.get(cacheKey)).thenReturn(null);

        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("errcode", 0);
        tokenResponse.put("errmsg", "ok");
        tokenResponse.put("access_token", "");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        assertThrows(BusinessException.class,
                () -> weWorkApiService.getAccessToken(WeWorkConstants.TOKEN_TYPE_APP));
    }

    @Test
    @DisplayName("发送文本消息 - 响应为空应抛出BusinessException")
    void sendTextMessage_nullResponse_shouldThrowBusinessException() {
        when(valueOperations.get(WeWorkConstants.TOKEN_CACHE_PREFIX + WeWorkConstants.TOKEN_TYPE_APP))
                .thenReturn("valid_token");

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class),
                any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        assertThrows(BusinessException.class,
                () -> weWorkApiService.sendTextMessage("ext_001", "测试"));
    }
}
