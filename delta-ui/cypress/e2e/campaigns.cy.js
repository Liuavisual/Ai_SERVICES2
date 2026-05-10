/// <reference types="cypress" />

describe('营销活动页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/campaigns')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示营销活动标题', () => {
    cy.contains('营销活动').should('exist')
  })

  it('应显示活动数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持活动类型和状态筛选', () => {
    cy.get('.el-select').should('have.length.at.least', 1)
    cy.contains('新建活动').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})