import { http } from './http'
import type { AuthSession, UserRole } from '../auth/session'

export interface ApiResponse<T> {
  code: string
  message: string
  data: T
}

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  role: UserRole
  expiresAt: number
}

export interface CreateOrderPayload {
  merchantNo: string
  merchantOrderNo: string
  subject: string
  amount: number
  callbackUrl?: string
}

export interface OrderItem {
  orderNo: string
  merchantNo: string
  merchantOrderNo: string
  subject: string
  amount: number
  status: string
  payUrl?: string
  expireAt?: string
  paidAt?: string
  createdAt: string
}

export interface PaymentResult {
  paymentNo: string | null
  orderNo: string
  status: string
  paidAt: string
}

export interface AdminPayment {
  paymentNo: string
  orderNo: string
  merchantNo: string
  amount: number
  status: string
  idempotencyKey: string
  paidAt?: string
  createdAt: string
}

export interface AdminEvent {
  eventId: string
  eventType: string
  aggregateNo: string
  payload: string
  status: string
  retryCount: number
  createdAt: string
}

export interface AdminNotification {
  notifyNo: string
  merchantNo: string
  orderNo: string
  callbackUrl: string
  status: string
  responseBody?: string
  retryCount: number
  nextRetryAt?: string
  createdAt: string
}

export interface ListQuery {
  keyword?: string
  status?: string
}

export interface EventListQuery extends ListQuery {
  eventType?: string
}

function unwrap<T>(response: ApiResponse<T>): T {
  if (response.code !== 'SUCCESS') {
    throw new Error(response.message || response.code)
  }
  return response.data
}

export function getErrorMessage(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) {
      return response.data.message
    }
  }
  if (error instanceof Error) {
    return error.message
  }
  return '请求失败'
}

export async function createMerchantOrder(payload: CreateOrderPayload): Promise<OrderItem> {
  const response = await http.post<ApiResponse<OrderItem>>('/merchant/orders', payload)
  return unwrap(response.data)
}

export async function login(payload: LoginPayload): Promise<AuthSession> {
  const response = await http.post<ApiResponse<LoginResponse>>('/auth/login', payload)
  return unwrap(response.data)
}

export async function listMerchantOrders(merchantNo: string, query: ListQuery = {}): Promise<OrderItem[]> {
  const response = await http.get<ApiResponse<OrderItem[]>>('/merchant/orders', {
    params: { merchantNo, ...query }
  })
  return unwrap(response.data)
}

export async function getPayOrder(orderNo: string): Promise<OrderItem> {
  const response = await http.get<ApiResponse<OrderItem>>(`/pay/orders/${orderNo}`)
  return unwrap(response.data)
}

export async function confirmPayment(order: OrderItem): Promise<PaymentResult> {
  const response = await http.post<ApiResponse<PaymentResult>>(`/pay/orders/${order.orderNo}/confirm`, {
    amount: order.amount,
    idempotencyKey: 'FRONTEND_CONFIRM'
  })
  return unwrap(response.data)
}

export async function listAdminOrders(query: ListQuery = {}): Promise<OrderItem[]> {
  const response = await http.get<ApiResponse<OrderItem[]>>('/admin/orders', { params: query })
  return unwrap(response.data)
}

export async function listAdminPayments(query: ListQuery = {}): Promise<AdminPayment[]> {
  const response = await http.get<ApiResponse<AdminPayment[]>>('/admin/payments', { params: query })
  return unwrap(response.data)
}

export async function listAdminEvents(query: EventListQuery = {}): Promise<AdminEvent[]> {
  const response = await http.get<ApiResponse<AdminEvent[]>>('/admin/events', { params: query })
  return unwrap(response.data)
}

export async function listAdminNotifications(query: ListQuery = {}): Promise<AdminNotification[]> {
  const response = await http.get<ApiResponse<AdminNotification[]>>('/admin/notifications', { params: query })
  return unwrap(response.data)
}
