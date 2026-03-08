/**
 * Auth module - Login form validation rules
 */

import * as v from 'valibot'

export const loginSchema = v.object({
  username: v.pipe(
    v.string('Username must be a string'),
    v.minLength(3, 'Username must be at least 3 characters'),
    v.maxLength(20, 'Username must be at most 20 characters'),
  ),
  password: v.pipe(
    v.string('Password must be a string'),
    v.minLength(6, 'Password must be at least 6 characters'),
  ),
  remember: v.optional(v.boolean()),
})

export type LoginSchema = v.InferOutput<typeof loginSchema>
