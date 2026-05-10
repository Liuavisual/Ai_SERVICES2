/// <reference types="cypress" />

describe('订单管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/orders')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示订单筛选表单', () => {
    cy.contains('订单状态').should('exist')
    cy.contains('支付状态').should('exist')
    cy.contains('订单号').should('exist')
  })

  it('订单状态筛选下拉框应包含所有选项', () => {
    cy.contains('订单状态').parent().find('.el-select').click()
    cy.get('.el-select-dropdown__item').should('have.length.at.least', 5)
  })

  it('支付状态筛选下拉框应包含选项', () => {
    cy.contains('支付状态').parent().find('.el-select').click()
    cy.get('.el-select-dropdown__item').should('have.length.at.least', 2)
  })

  it('按订单号搜索应正常工作', () => {
    cy.contains('订单号').parent().find('input').type('TEST001')
    cy.contains('查询').click()
    cy.get('.table-card').should('exist')
  })

  it('应显示订单数据表格', () => {
    cy.get('.el-table').should('exist')
    cy.contains('订单号').should('exist')
    cy.contains('订单管理').should('exist')
  })
})