import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'
import { useUserStore } from './stores/user'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

// Axios Request Interceptor
const userStore = useUserStore(pinia)
axios.interceptors.request.use(config => {
    if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
})

// Axios Response Interceptor
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            userStore.logout()
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

app.use(router)
app.use(ElementPlus)
app.mount('#app')
