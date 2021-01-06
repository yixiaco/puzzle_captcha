/**
 * 公共API
 */
import request from '@/utils/request'

/**
 * 验证码
 * @returns {*}
 */
export function captcha() {
  return request({
    url: `/captcha/`,
    loading: false,
    method: 'get'
  })
}

/**
 * 验证码验证
 * @returns {*}
 */
export function captchaVerify(data) {
  return request({
    url: `/captcha/verify`,
    method: 'post',
    headers: { 'Content-Type': 'application/json' },
    loading: false,
    data
  })
}
