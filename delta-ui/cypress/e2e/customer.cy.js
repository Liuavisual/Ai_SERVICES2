/// <reference types="cypress" />

describe('客户管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/customer')
    cy.get('.customer-page', { timeout: 10000 }).should('exist')
  })

  it('应显示筛选表单', () => {
    cy.get('.filter-card').should('exist')
    cy.contains('平台').should('exist')
    cy.contains('昵称').should('exist')
    cy.contains('查询').should('exist')
  })

  it('平台筛选下拉框应包含选项', () => {
    cy.contains('平台').parent().find('.el-select').click()
    cy.get('.el-select-dropdown__item').should('have.length.at.least', 2)
  })

  it('输入昵称可进行搜索', () => {
    cy.contains('昵称').parent().find('input').type('测试')
    cy.contains('查询').click()
    cy.get('.table-card').should('exist')
  })

  it('应显示客户数据表格', () => {
    cy.get('.el-table').should('exist')
    cy.contains('序号').should('exist')
  })
})