type MessageType = 'info' | 'warning' | 'success' | 'error'

interface MessageOptions {
  /** Unique identifier for the message, used to update or remove specific messages */
  id?: string
  /** Message title */
  title: string
  /** Message description */
  description?: string
  /** Message display duration (ms), defaults to 3000ms */
  duration?: number
}

interface MessageUpdateOptions {
  /** Message title */
  title?: string
  /** Message description */
  description?: string
  /** Message display duration (ms), defaults to 3000ms */
  duration?: number
}

/**
 * Message utility class
 * Wraps useToast, provides info, warning, success, error four message types
 */
export const useMessage = () => {
  const toast = useToast()

  /**
   * Send info message
   */
  const info = (options: MessageOptions | string) => {
    const opts = typeof options === 'string' ? { title: options } : options
    toast.add({
      id: opts.id,
      title: opts.title,
      description: opts.description,
      color: 'info',
      icon: 'i-lucide-circle-alert',
      duration: opts.duration,
      close: true,
    })
  }

  /**
   * Send warning message
   */
  const warning = (options: MessageOptions | string) => {
    const opts = typeof options === 'string' ? { title: options } : options
    toast.add({
      id: opts.id,
      title: opts.title,
      description: opts.description,
      color: 'warning',
      icon: 'i-lucide-circle-alert',
      duration: opts.duration,
      close: true,
    })
  }

  /**
   * Send success message
   */
  const success = (options: MessageOptions | string) => {
    const opts = typeof options === 'string' ? { title: options } : options
    toast.add({
      id: opts.id,
      title: opts.title,
      description: opts.description,
      color: 'success',
      icon: 'i-lucide-circle-check',
      duration: opts.duration,
      close: true,
    })
  }

  /**
   * Send error message
   */
  const error = (options: MessageOptions | string) => {
    const opts = typeof options === 'string' ? { title: options } : options
    toast.add({
      id: opts.id,
      title: opts.title,
      description: opts.description,
      color: 'error',
      icon: 'i-lucide-circle-alert',
      duration: opts.duration,
    })
  }

  /**
   * Send message by type
   * @param type Message type: info, warning, success, error
   * @param options Message options or message title string
   */
  const open = (type: MessageType, options: MessageOptions | string) => {
    switch (type) {
      case 'info':
        info(options)
        break
      case 'warning':
        warning(options)
        break
      case 'success':
        success(options)
        break
      case 'error':
        error(options)
        break
    }
  }

  /**
   * Update specified message
   * @param id Unique identifier of the message
   * @param options Update options
   */
  const update = (id: string, options: MessageUpdateOptions) => {
    toast.update(id, {
      title: options.title,
      description: options.description,
      duration: options.duration,
    })
  }

  /**
   * Remove specified message
   * @param id Unique identifier of the message
   */
  const remove = (id: string) => {
    toast.remove(id)
  }

  /**
   * Clear all messages
   */
  const clear = () => {
    toast.clear()
  }

  return {
    open,
    info,
    warning,
    success,
    error,
    update,
    remove,
    clear,
  }
}
