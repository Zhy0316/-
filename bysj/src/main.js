import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { createPinia } from 'pinia'
import { useUserStore } from './stores/user'

const app = createApp(App)
const pinia = createPinia()

app.use(ElementPlus)
app.use(pinia)
app.use(router)

// 应用启动时从 localStorage 恢复登录状态，必须在 pinia 注册后调用
useUserStore().loadFromStorage()

app.mount('#app')
