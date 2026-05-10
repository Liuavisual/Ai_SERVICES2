/// <reference types="cypress" />

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * 用户登录辅助命令
       */
      login(username?: string, password?: string): Chainable<any>
      /**
       * 用户登出辅助命令
       */
      logout(): Chainable<void>
    }
  }
}

// 用户登录辅助命令
Cypress.Commands.add('login', (username = 'admin', password = 'Admin@123456') => {
  cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/auth/login`,
    body: { username, password }
  }).then((response) => {
    expect(response.status).to.eq(200)
    return response.body
  })
})

// 用户登出辅助命令
Cypress.Commands.add('logout', () => {
  cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/auth/logout`
  })
})