/// <reference types="cypress" />

describe('关键词管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/keywords')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示关键词管理界面', () => {
    cy.contains('关键词').should('exist')
  })

  it('应显示关键词数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有搜索筛选区域', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应有新建关键词按钮', () => {
    cy.contains('新建').should('exist')
  })
})