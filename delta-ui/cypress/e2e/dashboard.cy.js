/// <reference types="cypress" />

describe('数据总览页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/dashboard')
    cy.get('.dash-page', { timeout: 10000 }).should('exist')
  })

  it('应显示数据总览标题', () => {
    cy.contains('数据总览').should('exist')
    cy.contains('实时运营数据监控').should('exist')
  })

  it('应显示统计卡片', () => {
    cy.get('.stat-card').should('have.length.at.least', 3)
  })

  it('时间周期切换应正常工作', () => {
    cy.get('.el-select').first().click()
    cy.get('.el-select-dropdown__item').contains('本周').click()
    cy.get('.dash-page').should('exist')
  })

  it('消息趋势区域应正常渲染', () => {
    cy.get('.trend-section').should('exist')
  })
})