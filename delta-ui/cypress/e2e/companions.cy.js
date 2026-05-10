/// <reference types="cypress" />

describe('陪玩师管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/companions')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示陪玩师管理界面', () => {
    cy.contains('陪玩师').should('exist')
  })

  it('应包含数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持搜索功能', () => {
    cy.get('input').should('have.length.at.least', 1)
  })

  it('数据表格应有表头', () => {
    cy.get('.el-table__header').should('exist')
  })
})