import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import UserDashboard from '../views/user/Dashboard.vue'
import CounselorDashboard from '../views/counselor/Dashboard.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/register', component: Register, meta: { public: true } },
  { path: '/user', component: UserDashboard, meta: { roles: ['USER', 'COUNSELOR', 'ADMIN'] } },
  { path: '/counselor', component: CounselorDashboard, meta: { roles: ['COUNSELOR'] } },
  { path: '/admin', component: AdminDashboard, meta: { roles: ['ADMIN'] } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const parseStoredUser = () => {
  try {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

const getDefaultHomeByRole = (role) => {
  if (role === 'ADMIN') return '/admin'
  if (role === 'COUNSELOR') return '/counselor'
  return '/user'
}

const refreshCurrentUser = async (user) => {
  if (!user?.id) return user
  try {
    const res = await axios.get(`/api/users/${user.id}`)
    if (res?.data?.code === 200 && res?.data?.data) {
      const latestUser = res.data.data
      localStorage.setItem('user', JSON.stringify(latestUser))
      return latestUser
    }
  } catch (e) {
    // Ignore refresh failures and keep existing local user snapshot.
  }
  return user
}

router.beforeEach(async (to) => {
  const token = localStorage.getItem('token')
  let user = parseStoredUser()

  if (token && !to.meta?.public) {
    user = await refreshCurrentUser(user)
  }

  const role = user?.role

  if (to.meta?.public) {
    if (token && role) {
      return getDefaultHomeByRole(role)
    }
    return true
  }

  if (!token || !role) {
    return '/login'
  }

  const allowedRoles = to.meta?.roles
  if (Array.isArray(allowedRoles) && !allowedRoles.includes(role)) {
    return getDefaultHomeByRole(role)
  }

  return true
})

export default router
