import Vue from 'vue'
import Router from 'vue-router'
import EditVoice from '@/components/ASR'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'ASR',
      component: EditVoice
    }
  ]
})
