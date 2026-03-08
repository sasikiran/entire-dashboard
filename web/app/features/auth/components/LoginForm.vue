<script setup lang="ts">
import { loginSchema } from '../schemas/login.schema'
import type { LoginForm } from '../types/auth.form'
import { useAuth } from '../composables/useAuth'

const { login, loading } = useAuth()

const form = reactive<LoginForm>({
  username: '',
  password: '',
  remember: false,
})

const handleSubmit = async () => {
  try {
    // Validate form
    const validatedData = await loginSchema.parseAsync(form)
    
    // Login
    await login({
      username: validatedData.username,
      password: validatedData.password,
    })
  } catch (error) {
    console.error('Login failed:', error)
  }
}
</script>

<template>
  <UCard class="w-full max-w-md">
    <template #header>
      <h2 class="text-2xl font-bold text-center">Login</h2>
    </template>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <UFormGroup label="Username" required>
        <UInput
          v-model="form.username"
          placeholder="Please enter username"
          :disabled="loading"
        />
      </UFormGroup>

      <UFormGroup label="Password" required>
        <UInput
          v-model="form.password"
          type="password"
          placeholder="Please enter password"
          :disabled="loading"
        />
      </UFormGroup>

      <UCheckbox
        v-model="form.remember"
        label="Remember me"
        :disabled="loading"
      />

      <UButton
        type="submit"
        block
        :loading="loading"
      >
        Login
      </UButton>
    </form>
  </UCard>
</template>
