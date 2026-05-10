/// <reference types="cypress" />

describe('陪玩师排班管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/companionSchedule')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示排班管理界面', () => {
    cy.contains('排班').should('exist')
  })

  it('应有日期选择功能', () => {
    cy.get('.el-date-picker').should('exist')
  })

  it('应显示排班数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('页面应正常渲染无报错', () => {
    cy.get('.page-container').should('exist')
  })
})