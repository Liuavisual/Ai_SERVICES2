/// <reference types="cypress" />

describe('服务项目管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/serviceItems')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示服务项目管理界面', () => {
    cy.contains('服务').should('exist')
  })

  it('应显示服务项目数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有搜索筛选功能', () => {
    cy.get('input').should('have.length.at.least', 1)
  })

  it('应有新建按钮', () => {
    cy.contains('新建').should('exist')
  })
})