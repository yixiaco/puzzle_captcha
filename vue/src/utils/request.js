import axios from 'axios'
import { Message, Loading } from 'element-ui'
import store from '@/store'

const qs = require('qs')
let loading

function request(api) {
  // 创建一个axios实例
  const service = axios.create({
    baseURL: api, // url = base url + request url
    // withCredentials: true, // 跨域请求时发送Cookie
    timeout: 8000 // 请求超时
  })

  // 请求拦截器
  service.interceptors.request.use(
    config => {
      // 如果是put/post请求，用qs.stringify序列化参数
      const is_put_post = config.method === 'put' || config.method === 'post'
      const is_json = config.headers['Content-Type'] === 'application/json'
      const is_file = config.headers['Content-Type'] === 'multipart/form-data'
      if (is_put_post && config.data) {
        if (is_json) {
          config.data = JSON.stringify(config.data)
        } else if (is_file) {
          const formData = new FormData()
          for (const key in config.data) {
            formData.append(key, config.data[key])
          }
          config.data = formData
        } else {
          config.data = qs.stringify(config.data, { arrayFormat: 'repeat' })
        }
      }
      // 在发送请求之前做一些事情
      if (store.getters.token) {
        // 让每个请求都带有令牌
        // ['Authorization'] 是自定义标题键
        // 请根据实际情况进行修改
        config.headers['Authorization'] = 'getToken'
      }
      if (config.loading !== false) {
        loading = Loading.service({
          lock: true,
          text: '处理中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })
      }
      return config
    },
    error => {
      loading && loading.close()
      // 做一些请求错误
      console.log(error) // 用于调试
      return Promise.reject(error)
    }
  )

  // 响应拦截器
  service.interceptors.response.use(
    /**
     * 如果要获取http信息（例如标题或状态）
     * 请返回响应=>响应
     */

    /**
     * Determine the request status by custom code
     * Here is just an example
     * You can also judge the status by HTTP Status Code
     */
    response => {
      loading && loading.close()
      let res = response.data
      if (response.request && response.request.responseType && response.request.responseType.toLowerCase() === 'blob') {
        res = response
      }
      // 如果自定义代码不是200，则将其判断为错误。
      if (response.status !== 200) {
        Message({
          message: res.message || 'Error',
          type: 'error',
          duration: 5 * 1000
        })
        return Promise.reject(new Error(res.message || 'Error'))
      } else {
        return res
      }
    },
    error => {
      loading && loading.close()
      const error_response = error.response || {}
      const error_data = error_response.data || {}
      const openMessage = true
      /* if (error_response.status === 401) {
        if (getToken()) {
          openMessage = false
          // 如果token过期，需要刷新一下token
          store.dispatch('user/refreshToken').then(token => {
            if (token) {
              location.reload()
            } else {
              store.dispatch('user/resetToken').then(() => {
                location.reload()
              })
            }
          })
        } else {
          store.dispatch('user/resetToken').then(() => {
            location.reload()
          })
        }
      }*/
      // console.log(error) // for debug
      if (openMessage && error.config.message !== false) {
        const _message = error.code === 'ECONNABORTED' ? '连接超时，请稍候再试！' : '网络错误，请稍后再试！'
        Message({
          message: error_data.message || _message,
          type: 'error',
          duration: 5 * 1000
        })
      }
      return Promise.reject(error)
    }
  )
  return service
}
// 导出默认请求
export default request(process.env.VUE_APP_BASE_API)
