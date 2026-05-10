/// <reference types="cypress" />

describe('用户登录页面', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('应显示登录表单', () => {
    cy.get('.login-container').should('exist')
    cy.contains('三角洲行动').should('exist')
  })

  it('空提交应显示校验错误', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('请输入用户名').should('exist')
  })

  it('错误密码应显示错误提示', () => {
    cy.get('input[placeholder*="用户名"]').type('admin')
    cy.get('input[placeholder*="密码"]').type('wrongpassword')
    cy.get('button[type="submit"]').click()
    cy.get('.login-error-banner').should('exist')
  })
})