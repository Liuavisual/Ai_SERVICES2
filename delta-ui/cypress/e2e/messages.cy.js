/// <reference types="cypress" />

describe('消息记录页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/messages')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示消息筛选表单', () => {
    cy.contains('平台').should('exist')
    cy.contains('方向').should('exist')
  })

  it('平台筛选下拉框应包含选项', () => {
    cy.contains('平台').parent().find('.el-select').first().click()
    cy.get('.el-select-dropdown__item').should('have.length.at.least', 2)
  })

  it('方向筛选下拉框应包含选项', () => {
    cy.contains('方向').parent().find('.el-select').click()
    cy.get('.el-select-dropdown__item').should('have.length.at.least', 1)
  })

  it('应显示消息数据区域', () => {
    cy.get('.filter-card').should('exist')
  })
})