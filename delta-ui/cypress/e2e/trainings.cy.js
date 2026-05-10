/// <reference types="cypress" />

describe('培训管理页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/trainings')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示培训管理标题', () => {
    cy.contains('培训管理').should('exist')
  })

  it('应显示培训数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持新增课程操作', () => {
    cy.contains('新增课程').should('exist')
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})