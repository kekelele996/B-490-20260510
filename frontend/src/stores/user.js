import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // Data Migration Logic
  const getInitialData = () => {
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')

    if (!storedUser) return { user: null, token: null }

    try {
      const parsed = JSON.parse(storedUser)
      // Check for old format: { user: {...}, token: "..." }
      if (parsed && parsed.user && parsed.token) {
        localStorage.setItem('user', JSON.stringify(parsed.user))
        localStorage.setItem('token', parsed.token)
        return { user: parsed.user, token: parsed.token }
      }
      return { user: parsed, token: storedToken }
    } catch (e) {
      return { user: null, token: null }
    }
  }

  const initialData = getInitialData()
  const user = ref(initialData.user)
  const token = ref(initialData.token)

  const setUser = (userData, userToken = null) => {
    user.value = userData
    if (userToken) {
      token.value = userToken
      localStorage.setItem('token', userToken)
    }
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return { user, token, setUser, logout }
})
