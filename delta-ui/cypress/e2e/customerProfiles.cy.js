/// <reference types="cypress" />

describe('客户画像页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/customer-profiles')
    cy.get('.profile-page', { timeout: 10000 }).should('exist')
  })

  it('应显示筛选卡片', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应支持RFM分群筛选', () => {
    cy.get('.el-select').should('have.length.at.least', 1)
  })

  it('应显示查询按钮', () => {
    cy.contains('查询').should('exist')
  })

  it('应包含客户画像数据', () => {
    cy.get('.profile-page').should('be.visible')
  })
})