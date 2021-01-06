import * as loginApi from '@/api/system/login'

const state = {
  token: '',
  name: '',
  avatar: '',
  introduction: '',
  roles: []
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_INTRODUCTION: (state, introduction) => {
    state.introduction = introduction
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  }
}

const actions = {
  /**
   * 用户名登录
   * @param commit
   * @param params
   * @returns {Promise<any>}
   */
  loginAction({ commit }, params) {
    return new Promise((resolve, reject) => {
      loginApi.login(params).then(response => {
        commit('SET_TOKEN', response.token)
        commit('SET_AVATAR', response.face)
        resolve()
      }).catch(reject)
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
