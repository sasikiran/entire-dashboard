<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950">
    <div class="text-center">
      <UIcon name="i-lucide-loader-circle" class="w-12 h-12 text-green-600 dark:text-green-500 animate-spin mx-auto mb-4"/>
      <p class="text-gray-600 dark:text-gray-400">Redirecting...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
})

const auth = useAuth()
const router = useRouter()

onMounted(() => {
  // Check login status
  if (auth.isAuthenticated) {
    // Already logged in, redirect to admin overview
    router.push('/admin/overview')
  } else {
    // Not logged in, redirect to login page
    router.push('/login')
  }
})
</script>
