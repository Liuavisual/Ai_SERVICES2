/// <reference types="cypress" />

describe('定价方案管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/pricing-plans')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示定价方案管理标题', () => {
    cy.contains('定价方案管理').should('exist')
  })

  it('应显示定价方案数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持新增方案操作', () => {
    cy.contains('新增方案').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})