/// <reference types="cypress" />

describe('AI配置管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/aiConfig')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示AI配置界面', () => {
    cy.contains('AI').should('exist')
  })

  it('应有配置表单区域', () => {
    cy.get('.el-form').should('exist')
  })

  it('应有保存配置按钮', () => {
    cy.contains('保存').should('exist')
  })

  it('页面应正常渲染无报错', () => {
    cy.get('.page-container').should('exist')
  })
})