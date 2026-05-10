/// <reference types="cypress" />

describe('游戏配置管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/gameConfigs')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示游戏配置界面', () => {
    cy.contains('游戏').should('exist')
  })

  it('应显示配置数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有搜索筛选功能', () => {
    cy.get('input').should('have.length.at.least', 1)
  })

  it('应有新建按钮', () => {
    cy.contains('新建').should('exist')
  })
})