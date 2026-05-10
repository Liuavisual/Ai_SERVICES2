/// <reference types="cypress" />

describe('结算管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/settlements')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示结算管理界面', () => {
    cy.contains('结算').should('exist')
  })

  it('应显示结算数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应有分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})