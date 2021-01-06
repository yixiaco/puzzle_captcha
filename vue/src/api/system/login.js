import request from '@/utils/request'

/**
 * 登录
 * @param data
 * @returns {*}
 */
export function login(data) {
  return request({
    url: 'system/login',
    method: 'post',
    loading: false,
    data
  })
}
