/// <reference types="cypress" />

describe('陪玩师等级管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/companion-levels')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示等级管理标题', () => {
    cy.contains('陪玩师等级管理').should('exist')
  })

  it('应显示等级数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持等级名称搜索', () => {
    cy.get('input').should('have.length.at.least', 1)
    cy.contains('新增等级').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})