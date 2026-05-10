/// <reference types="cypress" />

describe('对话试炼页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/chat-test')
    cy.get('.chat-test-container', { timeout: 10000 }).should('exist')
  })

  it('应显示通讯终端标题', () => {
    cy.contains('通讯终端').should('exist')
  })

  it('应显示AI Chat标签', () => {
    cy.contains('AI Chat').should('exist')
  })

  it('应显示客户昵称输入框', () => {
    cy.get('input').should('have.length.at.least', 1)
  })

  it('应支持重置操作', () => {
    cy.contains('重置').should('exist')
  })
})