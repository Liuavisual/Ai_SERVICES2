/// <reference types="cypress" />

describe('权限管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/permission')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示权限管理界面', () => {
    cy.contains('权限').should('exist')
  })

  it('应显示角色或权限数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有一级导航标签', () => {
    cy.get('.el-tabs').should('exist')
  })

  it('页面应有操作按钮区域', () => {
    cy.get('button').should('have.length.at.least', 1)
  })
})