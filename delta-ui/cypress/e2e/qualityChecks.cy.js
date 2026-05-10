/// <reference types="cypress" />

describe('质检记录页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/quality-checks')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示质检记录标题', () => {
    cy.contains('质检记录').should('exist')
  })

  it('应显示质检数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持风险等级筛选', () => {
    cy.get('.el-select').should('have.length.at.least', 1)
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})