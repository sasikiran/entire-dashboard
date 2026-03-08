/**
 * Common logic for form modal
 * @param defaultState Default state of the form
 * @returns Returns modal open/close state, form state, reset function, etc.
 */
export function useModalForm<T extends Record<string, any>>(defaultState: T) {
  // Control modal open/close
  const open = ref(false)

  // Form state
  const state = reactive<T>({ ...defaultState } as T)

  // Reset form function
  function resetForm() {
    Object.assign(state, defaultState)
  }

  // Watch for dialog close, clear form
  watch(open, (newValue: boolean) => {
    if (!newValue) {
      resetForm()
    }
  })

  return {
    open,
    state,
    resetForm,
  }
}
