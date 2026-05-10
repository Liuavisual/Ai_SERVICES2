/// <reference types="cypress" />

describe('活动套餐管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/activityPackages')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示活动套餐管理界面', () => {
    cy.contains('活动').should('exist')
  })

  it('应显示套餐数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有搜索筛选功能', () => {
    cy.get('input').should('have.length.at.least', 1)
  })

  it('应有新建按钮', () => {
    cy.contains('新建').should('exist')
  })
})