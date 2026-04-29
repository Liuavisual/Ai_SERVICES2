import { describe, it, expect } from 'vitest'
import type { Result, PageResult, UserRole, WorkOrderStatus, OrderStatus } from '@/types'

describe('Type Definitions', () => {
  it('Result type should be usable', () => {
    const result: Result<string> = { code: 200, message: 'ok', data: 'test' }
    expect(result.code).toBe(200)
    expect(result.data).toBe('test')
  })

  it('PageResult type should be usable', () => {
    const page: PageResult<string> = {
      records: ['a', 'b'],
      total: 2,
      size: 10,
      current: 1,
      pages: 1,
    }
    expect(page.records).toHaveLength(2)
    expect(page.total).toBe(2)
  })

  it('UserRole type should accept valid values', () => {
    const roles: UserRole[] = ['SYS_ADMIN', 'CS_LEADER', 'CS_STAFF']
    expect(roles).toHaveLength(3)
  })

  it('WorkOrderStatus type should accept valid values', () => {
    const statuses: WorkOrderStatus[] = ['OPEN', 'IN_PROGRESS', 'SUBMITTED', 'CONFIRMED', 'CLOSED', 'CANCELLED']
    expect(statuses).toHaveLength(6)
  })

  it('OrderStatus type should accept valid values', () => {
    const statuses: OrderStatus[] = ['PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
    expect(statuses).toHaveLength(5)
  })
})
