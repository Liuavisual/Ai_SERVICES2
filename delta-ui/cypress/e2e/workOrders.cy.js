/// <reference types="cypress" />

describe('工单管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/workOrders')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示工单管理界面', () => {
    cy.contains('工单').should('exist')
  })

  it('应包含工单数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('工单数据表格应有标题列', () => {
    cy.get('.el-table__header').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})