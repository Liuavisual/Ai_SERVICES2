/// <reference types="cypress" />

describe('自动回复管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/replies')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示自动回复管理界面', () => {
    cy.contains('回复').should('exist')
  })

  it('应显示回复数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应有分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})