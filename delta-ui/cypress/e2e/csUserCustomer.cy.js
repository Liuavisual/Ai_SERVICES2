/// <reference types="cypress" />

describe('客服-客户分配页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/cs-user-customer')
    cy.get('.cs-user-customer-container', { timeout: 10000 }).should('exist')
  })

  it('应显示客户分配管理标题', () => {
    cy.contains('客服-客户分配管理').should('exist')
  })

  it('应显示分配数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持状态筛选', () => {
    cy.get('.el-select').should('exist')
    cy.contains('查询').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})