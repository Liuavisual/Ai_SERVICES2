/// <reference types="cypress" />

describe('排班日历视图页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/companion-schedule-calendar')
    cy.get('.schedule-calendar-page', { timeout: 10000 }).should('exist')
  })

  it('应显示排班日历标题', () => {
    cy.contains('排班日历').should('exist')
  })

  it('应显示陪玩师选择器', () => {
    cy.get('.el-select').should('exist')
  })

  it('应显示月份导航按钮', () => {
    cy.contains('今天').should('exist')
  })

  it('应显示日历网格', () => {
    cy.get('.calendar-grid').should('exist')
  })
})