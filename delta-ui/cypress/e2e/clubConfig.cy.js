/// <reference types="cypress" />

describe('俱乐部配置页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/club-config')
    cy.get('.club-config-container', { timeout: 10000 }).should('exist')
  })

  it('应显示俱乐部配置标题', () => {
    cy.contains('俱乐部配置').should('exist')
  })

  it('应显示配置表单', () => {
    cy.get('.el-form').should('exist')
  })

  it('应包含基础信息区域', () => {
    cy.contains('基础信息').should('exist')
  })

  it('应显示保存配置按钮', () => {
    cy.contains('保存配置').should('exist')
  })
})