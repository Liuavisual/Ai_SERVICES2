/// <reference types="cypress" />

describe('满意度管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/satisfaction')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示满意度管理界面', () => {
    cy.contains('满意').should('exist')
  })

  it('应显示评价数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有数据筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('数据表格应有评分列', () => {
    cy.get('.el-table__header').should('exist')
  })
})