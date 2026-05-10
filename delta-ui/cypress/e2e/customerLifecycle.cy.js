/// <reference types="cypress" />

describe('客户生命周期管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/customerLifecycle')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示客户生命周期界面', () => {
    cy.contains('生命周期').should('exist')
  })

  it('应显示客户数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有阶段筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应有分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})