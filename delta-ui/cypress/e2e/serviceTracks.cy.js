/// <reference types="cypress" />

describe('服务追踪页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/service-tracks')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示查询栏', () => {
    cy.get('.filter-card').should('exist')
  })

  it('应支持按用户ID或订单ID查询', () => {
    cy.get('.el-select').should('exist')
    cy.get('input').should('have.length.at.least', 1)
  })

  it('应显示查询和重置按钮', () => {
    cy.contains('查询').should('exist')
    cy.contains('重置').should('exist')
  })

  it('应显示服务追踪列表', () => {
    cy.get('.page-container').should('be.visible')
  })
})