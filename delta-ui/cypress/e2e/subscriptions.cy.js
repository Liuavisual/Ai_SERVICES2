/// <reference types="cypress" />

describe('订阅管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/subscriptions')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示订阅管理标题', () => {
    cy.contains('订阅管理').should('exist')
  })

  it('应显示订阅数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持订阅状态筛选', () => {
    cy.get('.el-select').should('exist')
    cy.contains('开通订阅').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})