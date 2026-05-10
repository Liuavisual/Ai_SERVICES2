/// <reference types="cypress" />

describe('待处理消息页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/pendingMessages')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示待处理消息界面', () => {
    cy.contains('待处理').should('exist')
  })

  it('应显示消息数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应有筛选功能', () => {
    cy.get('.filter-card').should('exist')
  })

  it('页面应正常渲染无报错', () => {
    cy.get('.page-container').should('exist')
  })
})