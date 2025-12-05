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
      
      /**
       * Custom command to mock Reservation API responses
       * @example cy.mockReservationAPI()
       */
      mockReservationAPI(): Chainable<void>
      
      /**
       * Custom command to login as admin with ROOM_ADMIN role
       * @example cy.loginAsAdmin()
       */
      loginAsAdmin(): Chainable<void>
      
      /**
       * Custom command to login as employee with EMPLOYEE role
       * @example cy.loginAsEmployee()
       */
      loginAsEmployee(): Chainable<void>
      
      /**
       * Custom command to logout
       * @example cy.logout()
       */
      logout(): Chainable<void>
      
      /**
       * Custom command to mock Room Management API responses
       * @example cy.mockRoomManagementAPI()
       */
      mockRoomManagementAPI(): Chainable<void>
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

// Mock Reservation API responses for US2
Cypress.Commands.add('mockReservationAPI', () => {
  // Get my reservations
  cy.intercept('GET', '/api/reservations/my', {
    statusCode: 200,
    body: [
      {
        id: 1,
        roomId: 1,
        roomName: '會議室 A',
        userId: 'test@example.com',
        startTime: new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString(), // 48 小時後
        endTime: new Date(Date.now() + 49 * 60 * 60 * 1000).toISOString(),
        purpose: '團隊會議',
        participants: ['張三', '李四'],
        status: 'CONFIRMED',
        createdAt: new Date().toISOString()
      },
      {
        id: 2,
        roomId: 2,
        roomName: '會議室 B',
        userId: 'test@example.com',
        startTime: new Date(Date.now() + 72 * 60 * 60 * 1000).toISOString(), // 72 小時後
        endTime: new Date(Date.now() + 73 * 60 * 60 * 1000).toISOString(),
        purpose: '客戶簡報',
        participants: ['王五', '趙六'],
        status: 'CONFIRMED',
        createdAt: new Date().toISOString()
      },
      {
        id: 3,
        roomId: 1,
        roomName: '會議室 A',
        userId: 'test@example.com',
        startTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), // 24 小時前
        endTime: new Date(Date.now() - 23 * 60 * 60 * 1000).toISOString(),
        purpose: '季度檢討',
        participants: ['張三'],
        status: 'COMPLETED',
        createdAt: new Date(Date.now() - 72 * 60 * 60 * 1000).toISOString()
      },
      {
        id: 4,
        roomId: 2,
        roomName: '會議室 B',
        userId: 'test@example.com',
        startTime: new Date(Date.now() + 96 * 60 * 60 * 1000).toISOString(), // 96 小時後
        endTime: new Date(Date.now() + 97 * 60 * 60 * 1000).toISOString(),
        purpose: '已取消的會議',
        participants: [],
        status: 'CANCELLED',
        createdAt: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString()
      }
    ]
  }).as('getMyReservations')

  // Get reservation detail
  cy.intercept('GET', '/api/reservations/*', (req) => {
    const id = parseInt(req.url.split('/').pop() || '1')
    req.reply({
      statusCode: 200,
      body: {
        id: id,
        roomId: 1,
        roomName: '會議室 A',
        userId: 'test@example.com',
        startTime: new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString(),
        endTime: new Date(Date.now() + 49 * 60 * 60 * 1000).toISOString(),
        purpose: '團隊會議',
        participants: ['張三', '李四'],
        status: 'CONFIRMED',
        createdAt: new Date().toISOString()
      }
    })
  }).as('getReservationDetail')

  // Update reservation
  cy.intercept('PUT', '/api/reservations/*', (req) => {
    const id = parseInt(req.url.split('/').pop() || '1')
    const body = req.body
    
    // Simulate conflict error for specific time
    if (body.startTime && body.startTime.includes('14:00')) {
      req.reply({
        statusCode: 409,
        body: {
          message: '時間衝突：該時段已有其他預約'
        }
      })
    } else {
      req.reply({
        statusCode: 200,
        body: {
          id: id,
          roomId: body.roomId || 1,
          roomName: '會議室 A',
          userId: 'test@example.com',
          startTime: body.startTime,
          endTime: body.endTime,
          purpose: body.purpose,
          participants: body.participants,
          status: 'CONFIRMED',
          updatedAt: new Date().toISOString()
        }
      })
    }
  }).as('updateReservation')

  // Cancel reservation
  cy.intercept('DELETE', '/api/reservations/*', (req) => {
    const id = parseInt(req.url.split('/').pop() || '1')
    
    // Simulate 24-hour rule violation for reservation id 2
    if (id === 2) {
      req.reply({
        statusCode: 400,
        body: {
          message: '無法取消：距離會議開始時間不足 24 小時'
        }
      })
    } else {
      req.reply({
        statusCode: 204
      })
    }
  }).as('cancelReservation')
})

// Mock login as admin with ROOM_ADMIN role
Cypress.Commands.add('loginAsAdmin', () => {
  const mockAdminToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkFkbWluIFVzZXIiLCJyb2xlIjoiUk9PTV9BRE1JTiIsImlhdCI6MTUxNjIzOTAyMn0.dummytoken'
  
  window.localStorage.setItem('auth_token', mockAdminToken)
  window.localStorage.setItem('user_email', 'admin@example.com')
  window.localStorage.setItem('user_name', 'Admin User')
  window.localStorage.setItem('user_role', 'ROOM_ADMIN')
})

// Mock login as employee with EMPLOYEE role
Cypress.Commands.add('loginAsEmployee', () => {
  const mockEmployeeToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkVtcGxveWVlIFVzZXIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJpYXQiOjE1MTYyMzkwMjJ9.dummytoken'
  
  window.localStorage.setItem('auth_token', mockEmployeeToken)
  window.localStorage.setItem('user_email', 'employee@example.com')
  window.localStorage.setItem('user_name', 'Employee User')
  window.localStorage.setItem('user_role', 'EMPLOYEE')
})

// Mock logout
Cypress.Commands.add('logout', () => {
  window.localStorage.clear()
  window.sessionStorage.clear()
})

// Mock Room Management API responses for US4
Cypress.Commands.add('mockRoomManagementAPI', () => {
  // Get all rooms for admin
  cy.intercept('GET', '/api/v1/rooms*', {
    statusCode: 200,
    body: {
      data: [
        {
          id: 1,
          name: '會議室 A',
          building: '總部大樓',
          floor: '3F',
          capacity: 10,
          locationDescription: '電梯旁',
          equipment: ['投影機', '白板', '視訊設備'],
          features: ['視訊會議', '無線投影'],
          photos: ['/images/room-a-1.jpg'],
          status: 'AVAILABLE'
        },
        {
          id: 2,
          name: '會議室 B',
          building: '總部大樓',
          floor: '3F',
          capacity: 20,
          equipment: ['投影機', '白板'],
          features: ['視訊會議'],
          photos: [],
          status: 'AVAILABLE'
        }
      ],
      total: 2
    }
  }).as('getRoomsForAdmin')

  // Create room
  cy.intercept('POST', '/api/v1/rooms', (req) => {
    req.reply({
      statusCode: 201,
      body: {
        id: 100,
        name: req.body.name,
        building: req.body.building,
        floor: req.body.floor,
        capacity: req.body.capacity,
        locationDescription: req.body.locationDescription,
        equipment: req.body.equipment,
        features: req.body.features,
        photos: [],
        status: 'AVAILABLE'
      }
    })
  }).as('createRoom')

  // Update room
  cy.intercept('PUT', '/api/v1/rooms/*', (req) => {
    const id = parseInt(req.url.split('/').pop() || '1')
    req.reply({
      statusCode: 200,
      body: {
        id: id,
        name: req.body.name,
        building: req.body.building,
        floor: req.body.floor,
        capacity: req.body.capacity,
        locationDescription: req.body.locationDescription,
        equipment: req.body.equipment,
        features: req.body.features,
        photos: req.body.photos || [],
        status: req.body.status || 'AVAILABLE'
      }
    })
  }).as('updateRoom')

  // Delete room
  cy.intercept('DELETE', '/api/v1/rooms/*', {
    statusCode: 204
  }).as('deleteRoom')

  // Upload photo
  cy.intercept('POST', '/api/v1/rooms/*/photos', {
    statusCode: 201,
    body: {
      photoUrl: '/images/uploaded-photo.jpg'
    }
  }).as('uploadPhoto')

  // Create maintenance schedule
  cy.intercept('POST', '/api/v1/admin/rooms/*/maintenance', (req) => {
    req.reply({
      statusCode: 201,
      body: {
        id: 1,
        roomId: parseInt(req.url.split('/')[5]),
        startTime: req.body.startTime,
        endTime: req.body.endTime,
        reason: req.body.reason,
        description: req.body.description,
        status: 'SCHEDULED'
      }
    })
  }).as('createMaintenance')

  // Get maintenance schedules
  cy.intercept('GET', '/api/v1/admin/rooms/*/maintenance', {
    statusCode: 200,
    body: []
  }).as('getMaintenanceSchedules')
})

export {}
