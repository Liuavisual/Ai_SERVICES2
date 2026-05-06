package com.delta.common.service;

import com.delta.common.entity.ClubConfig;
import com.delta.common.entity.FaqItem;
import com.delta.common.service.matcher.KeywordMatcherService;
import com.delta.common.vo.CompanionLevelVO;
import com.delta.common.vo.ServiceItemVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存服务，使用双重检查锁定（Double-Checked Locking）保证线程安全
 * <p>
 * 采用 volatile 字段 + synchronized 双重检查的模式，
 * 确保多线程环境下的缓存可见性和单次加载。
 * </p>
 *
 * @author delta
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final KeywordMatcherService keywordMatcherService;

    private final ClubConfigService clubConfigService;

    private final FaqItemService faqItemService;

    private final CompanionLevelService companionLevelService;

    private final ServiceItemService serviceItemService;

    private final Object clubConfigLock = new Object();
    private final Object faqItemsLock = new Object();
    private final Object companionLevelsLock = new Object();
    private final Object serviceItemsLock = new Object();

    private volatile ClubConfig cachedClubConfig = null;
    private volatile List<FaqItem> cachedFaqItems = null;
    private volatile List<CompanionLevelVO> cachedCompanionLevels = null;
    private volatile List<ServiceItemVO> cachedServiceItems = null;
    private volatile ConcurrentHashMap<String, String> cachedAiConfig = new ConcurrentHashMap<>();

    public void reloadKeywords() {
        log.info("刷新关键词缓存...");
        keywordMatcherService.refreshKeywords();
        log.info("关键词缓存刷新完成");
    }

    public void reloadReplies() {
        log.info("回复话术缓存刷新完成（暂未实现）");
    }

    public void reloadAiConfig() {
        log.info("刷新AI配置缓存...");
        cachedAiConfig.clear();
        log.info("AI配置缓存刷新完成");
    }

    public void reloadClubConfig() {
        log.info("刷新俱乐部配置缓存...");
        synchronized (clubConfigLock) {
            cachedClubConfig = clubConfigService.getClubConfig();
        }
        log.info("俱乐部配置缓存刷新完成");
    }

    public void reloadFaqItems() {
        log.info("刷新FAQ知识库缓存...");
        synchronized (faqItemsLock) {
            cachedFaqItems = faqItemService.getEnabledFaqItems();
        }
        log.info("FAQ知识库缓存刷新完成，共 {} 条", cachedFaqItems != null ? cachedFaqItems.size() : 0);
    }

    public void reloadCompanionLevels() {
        log.info("刷新陪玩等级缓存...");
        synchronized (companionLevelsLock) {
            cachedCompanionLevels = companionLevelService.getAllEnabled();
        }
        log.info("陪玩等级缓存刷新完成，共 {} 个等级", cachedCompanionLevels != null ? cachedCompanionLevels.size() : 0);
    }

    public void reloadServiceItems() {
        log.info("刷新服务项目缓存...");
        ClubConfig clubConfig = getClubConfig();
        if (clubConfig != null) {
            synchronized (serviceItemsLock) {
                cachedServiceItems = serviceItemService.getByClubId(clubConfig.getId());
            }
        }
        log.info("服务项目缓存刷新完成，共 {} 个项目", cachedServiceItems != null ? cachedServiceItems.size() : 0);
    }

    public ClubConfig getClubConfig() {
        if (cachedClubConfig == null) {
            synchronized (clubConfigLock) {
                if (cachedClubConfig == null) {
                    cachedClubConfig = clubConfigService.getClubConfig();
                }
            }
        }
        return cachedClubConfig;
    }

    public List<FaqItem> getFaqItems() {
        if (cachedFaqItems == null) {
            synchronized (faqItemsLock) {
                if (cachedFaqItems == null) {
                    cachedFaqItems = faqItemService.getEnabledFaqItems();
                }
            }
        }
        return cachedFaqItems;
    }

    public List<CompanionLevelVO> getCompanionLevels() {
        if (cachedCompanionLevels == null) {
            synchronized (companionLevelsLock) {
                if (cachedCompanionLevels == null) {
                    cachedCompanionLevels = companionLevelService.getAllEnabled();
                }
            }
        }
        return cachedCompanionLevels;
    }

    public List<ServiceItemVO> getServiceItems() {
        if (cachedServiceItems == null) {
            synchronized (serviceItemsLock) {
                if (cachedServiceItems == null) {
                    ClubConfig clubConfig = getClubConfig();
                    if (clubConfig != null) {
                        cachedServiceItems = serviceItemService.getByClubId(clubConfig.getId());
                    }
                }
            }
        }
        return cachedServiceItems;
    }

    public String getAiConfigValue(String key) {
        return cachedAiConfig.get(key);
    }

    public void putAiConfigValue(String key, String value) {
        cachedAiConfig.put(key, value);
    }

    public void initAllCaches() {
        log.info("开始初始化所有缓存...");
        reloadKeywords();
        reloadClubConfig();
        reloadFaqItems();
        reloadCompanionLevels();
        reloadServiceItems();
        reloadAiConfig();
        log.info("所有缓存初始化完成");
    }
}
