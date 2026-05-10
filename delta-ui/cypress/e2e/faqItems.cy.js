/// <reference types="cypress" />

describe('FAQ问题管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/faqItems')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示FAQ管理界面', () => {
    cy.contains('FAQ').should('exist')
  })

  it('应显示问题数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有分类筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应有新建FAQ按钮', () => {
    cy.contains('新建').should('exist')
  })
})