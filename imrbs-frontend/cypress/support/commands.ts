/// <reference types="cypress" />

// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Custom command to login with mock token
       * @example cy.login()
       */
      login(): Chainable<void>
      
      /**
       * Custom command to mock API responses
       * @example cy.mockRoomAPI()
       */
      mockRoomAPI(): Chainable<void>
    }
  }
}

// Mock login - 設置 JWT token
Cypress.Commands.add('login', () => {
  // Mock JWT token (for testing purposes)
  const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRlc3QgVXNlciIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c'
  
  window.localStorage.setItem('auth_token', mockToken)
  window.localStorage.setItem('user_email', 'test@example.com')
  window.localStorage.setItem('user_name', 'Test User')
})

// Mock Room API responses
Cypress.Commands.add('mockRoomAPI', () => {
  // Intercept API calls and return mock data
  cy.intercept('GET', '/api/v1/rooms*', {
    statusCode: 200,
    body: {
      data: [
        {
          id: 1,
          name: '會議室 A',
          capacity: 10,
          floor: 3,
          building: '總部大樓',
          equipment: ['投影機', '白板', '視訊設備'],
          features: {},
          photos: [],
          status: 'AVAILABLE'
        },
        {
          id: 2,
          name: '會議室 B',
          capacity: 20,
          floor: 3,
          building: '總部大樓',
          equipment: ['投影機', '白板'],
          features: {},
          photos: [],
          status: 'AVAILABLE'
        }
      ],
      total: 2
    }
  }).as('getRooms')

  cy.intercept('GET', '/api/v1/rooms/1', {
    statusCode: 200,
    body: {
      id: 1,
      name: '會議室 A',
      capacity: 10,
      floor: 3,
      building: '總部大樓',
      equipment: ['投影機', '白板', '視訊設備'],
      features: {},
      photos: [],
      status: 'AVAILABLE'
    }
  }).as('getRoomById')

  cy.intercept('GET', '/api/v1/rooms/1/availability*', {
    statusCode: 200,
    body: {
      date: '2025-11-21',
      roomId: 1,
      availableSlots: [
        {
          startTime: '2025-11-21T09:00:00',
          endTime: '2025-11-21T10:00:00',
          available: true
        },
        {
          startTime: '2025-11-21T10:00:00',
          endTime: '2025-11-21T11:00:00',
          available: true
        },
        {
          startTime: '2025-11-21T14:00:00',
          endTime: '2025-11-21T15:00:00',
          available: true
        }
      ]
    }
  }).as('getRoomAvailability')

  cy.intercept('POST', '/api/v1/reservations', {
    statusCode: 201,
    body: {
      id: 123,
      roomId: 1,
      userId: 'test@example.com',
      startTime: '2025-11-21T09:00:00',
      endTime: '2025-11-21T10:00:00',
      purpose: '團隊會議',
      status: 'CONFIRMED'
    }
  }).as('createReservation')
})

export {}
