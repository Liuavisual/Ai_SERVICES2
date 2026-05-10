/// <reference types="cypress" />

describe('系统用户管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/sysUsers')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示用户管理标题', () => {
    cy.contains('用户').should('exist')
  })

  it('应显示用户数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持用户名搜索', () => {
    cy.get('input').should('have.length.at.least', 1)
    cy.contains('查询').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})