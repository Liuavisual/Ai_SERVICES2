/// <reference types="cypress" />

describe('报表统计页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/reports')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示报表界面', () => {
    cy.contains('报表').should('exist')
  })

  it('应有数据筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应有时间范围选择器', () => {
    cy.get('.el-date-picker').should('exist')
  })

  it('页面应正常渲染无报错', () => {
    cy.get('.page-container').should('exist')
  })
})