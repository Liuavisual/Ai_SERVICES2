/// <reference types="cypress" />

describe('裂变推荐页面', () => {
  beforeEach(() => {
    cy.login()
    cy.visit('/referrals')
    cy.get('.page-container', { timeout: 10000 }).should('exist')
  })

  it('应显示裂变推荐标题', () => {
    cy.contains('裂变推荐').should('exist')
  })

  it('应显示推荐数据表格', () => {
    cy.get('.el-table').should('exist')
  })

  it('应支持转化状态和奖励状态筛选', () => {
    cy.get('.el-select').should('have.length.at.least', 1)
  })

  it('应包含分页组件', () => {
    cy.get('.el-pagination').should('exist')
  })
})