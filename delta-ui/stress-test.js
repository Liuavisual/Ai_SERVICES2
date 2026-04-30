/**
 * Delta AI客服平台 - k6压力测试脚本
 *
 * 测试场景：
 * 1. 正常负载：50并发用户，持续5分钟
 * 2. 峰值负载：200并发用户，持续3分钟
 * 3. 极限负载：500并发用户，持续2分钟
 *
 * 使用方式：k6 run stress-test.js
 * 需要先启动后端服务：http://localhost:8080
 *
 * @author 刘建国
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/** 自定义指标 */
const errorRate = new Rate('errors');
const loginTrend = new Trend('login_duration', true);
const pageQueryTrend = new Trend('page_query_duration', true);
const crudTrend = new Trend('crud_duration', true);

/** 基础配置 */
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';

/** 测试阶段配置 - 3000并发极限压力测试 */
export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 200 },
    { duration: '30s', target: 200 },
    { duration: '1m', target: 500 },
    { duration: '30s', target: 500 },
    { duration: '1m', target: 1000 },
    { duration: '30s', target: 1000 },
    { duration: '1m', target: 2000 },
    { duration: '30s', target: 2000 },
    { duration: '1m', target: 3000 },
    { duration: '2m', target: 3000 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000', 'p(99)<8000'],
    errors: ['rate<0.15'],
    login_duration: ['p(95)<5000', 'p(99)<10000'],
    page_query_duration: ['p(95)<2000', 'p(99)<5000'],
    crud_duration: ['p(95)<3000', 'p(99)<6000'],
  },
};

/** 登录获取Token */
function login() {
  const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    username: 'admin',
    password: 'admin123456',
  }), {
    headers: { 'Content-Type': 'application/json' },
    timeout: '10s',
  });
  loginTrend.add(res.timings.duration);
  const success = check(res, {
    '登录状态码200': (r) => r.status === 200,
    '返回Token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.token;
      } catch { return false; }
    },
  });
  if (!success) { errorRate.add(1); return null; }
  try {
    return JSON.parse(res.body).data.token;
  } catch { return null; }
}

/** 带认证的请求头 */
function authHeaders(token) {
  return {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    timeout: '10s',
  };
}

/** 分页查询测试 */
function testPageQuery(token) {
  const endpoints = [
    { url: '/keywords/page?page=1&size=10', name: '关键词分页' },
    { url: '/replies/page?page=1&size=10', name: '回复分页' },
    { url: '/messages/page?page=1&size=20', name: '消息分页' },
    { url: '/pending-messages/page?page=1&size=20', name: '待处理消息分页' },
    { url: '/customers/page?page=1&size=10', name: '客户分页' },
    { url: '/companions/page?page=1&size=10', name: '陪玩师分页' },
    { url: '/companion-levels/page?page=1&size=10', name: '等级分页' },
    { url: '/orders/query?page=1&size=10', name: '订单分页' },
    { url: '/work-orders/page?page=1&size=10', name: '工单分页' },
    { url: '/sys-users/page?page=1&size=10', name: '用户分页' },
    { url: '/satisfaction/page?page=1&size=10', name: '满意度分页' },
    { url: '/faq-items?page=1&size=10', name: 'FAQ分页' },
  ];
  const ep = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(`${BASE_URL}${ep.url}`, authHeaders(token));
  pageQueryTrend.add(res.timings.duration);
  const success = check(res, {
    [`${ep.name}状态码200`]: (r) => r.status === 200,
    [`${ep.name}返回数据`]: (r) => {
      try { return JSON.parse(r.body).code === 200 || JSON.parse(r.body).code === 0; }
      catch { return false; }
    },
  });
  if (!success) errorRate.add(1);
}

/** CRUD操作测试 */
function testCRUD(token) {
  const keywordRes = http.post(`${BASE_URL}/keywords`, JSON.stringify({
    keyword: `压力测试_${Date.now()}`,
    replyContent: '压力测试自动回复',
    matchType: 'EXACT',
    enabled: 1,
  }), authHeaders(token));
  crudTrend.add(keywordRes.timings.duration);
  const createOk = check(keywordRes, {
    '创建关键词状态码200': (r) => r.status === 200,
  });
  if (!createOk) { errorRate.add(1); return; }
  try {
    const body = JSON.parse(keywordRes.body);
    if (body.data && body.data.id) {
      const delRes = http.del(`${BASE_URL}/keywords/${body.data.id}`, null, authHeaders(token));
      check(delRes, { '删除关键词状态码200': (r) => r.status === 200 });
    }
  } catch { errorRate.add(1); }
}

/** 统计接口测试 */
function testStats(token) {
  const endpoints = ['/stats/personal', '/stats/team', '/stats/global'];
  const ep = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(`${BASE_URL}${ep}`, authHeaders(token));
  check(res, { '统计接口状态码200': (r) => r.status === 200 || r.status === 403 });
}

/** 主测试逻辑 */
export default function () {
  const token = login();
  if (!token) { sleep(1); return; }
  const action = Math.random();
  if (action < 0.6) {
    testPageQuery(token);
  } else if (action < 0.85) {
    testCRUD(token);
  } else {
    testStats(token);
  }
  sleep(Math.random() * 2 + 0.5);
}

/** 测试摘要输出 */
export function handleSummary(data) {
  const metrics = {};
  for (const [name, m] of Object.entries(data.metrics)) {
    metrics[name] = {
      values: m.values,
      thresholds: m.thresholds ? Object.fromEntries(
        Object.entries(m.thresholds).map(([k, v]) => [k, v.ok])
      ) : undefined,
    };
  }
  const report = {
    testRun: new Date().toISOString(),
    scenarios: Object.keys(data.options?.scenarios || {}),
    metrics,
  };
  return {
    stdout: JSON.stringify(report, null, 2),
    'd:/Project/AI-SERVERS/sql/stress-test-report.json': JSON.stringify(report, null, 2),
  };
}
