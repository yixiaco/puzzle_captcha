<template>
  <div class="login-container">
    <div id="login-bg" class="login-bg" />
    <div class="login-form" @keyup.enter="submitLogin">
      <!--      <el-image :src="require('@/assets/logo_images/logo-white.png')" fit="contain" style="width: 300px; height: 120px" />-->
      <div class="title">{{ title }}</div>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-input">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="账号" autofocus clearable>
            <svg-icon slot="prefix" class="el-input__icon" icon-class="user" />
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" :minlength="6" :maxlength="20" clearable>
            <svg-icon slot="prefix" class="el-input__icon" icon-class="password" />
          </el-input>
        </el-form-item>
        <el-form-item prop="vcode">
          <!--滑动验证码-->
          <Vcode :complete="loginForm.vcode" @success="success" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submitLogin">登陆</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import 'particles.js'
import particlesjsConfig from '@/assets/particlesjs-config.json'
import Vcode from '@/components/Vcode'
import { title } from '@/settings'

export default {
  name: 'Login',
  components: {
    Vcode
  },
  data() {
    return {
      title,
      loading: false,
      loginForm: {
        username: '',
        password: '',
        vcode: false,
        vcodeTime: 0
      },
      loginRules: {
        username: [
          {
            required: true,
            message: '用户名不能为空',
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: true,
            message: '密码不能为空',
            trigger: 'blur'
          }
        ],
        vcode: [{
          validator: (rule, value, callback) => {
            if (!value) {
              callback(new Error('请完成图片验证'))
            } else {
              const time = new Date().getTime() - this.loginForm.vcodeTime
              // 超时3分钟就重新验证吧
              if (time / 1000 / 60 > 3) {
                this.loginForm.vcode = false
                callback(new Error('请完成图片验证'))
              }
              callback()
            }
          },
          trigger: 'change'
        }]
      },
      redirect: undefined,
      otherQuery: {}
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        const query = route.query
        if (query) {
          this.redirect = query.redirect
          this.otherQuery = this.getOtherQuery(query)
        }
      },
      immediate: true
    }
  },
  mounted() {
    this.loadParticles()
  },
  methods: {
    // 用户通过了验证
    success(x) {
      this.loginForm.vcode = true
      this.loginForm.vcodeTime = new Date().getTime()
    },
    /** 加载背景插件 */
    loadParticles() {
      window.particlesJS('login-bg', particlesjsConfig)
    },
    /** 表单提交 */
    submitLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          const params = { ...this.loginForm }
          params.uuid = this.uuid
          this.loading = true
          this.$store
            .dispatch('user/loginAction', params)
            .then(() => {
              this.loading = false
              // const forward = this.$route.query.forward
              this.$router.push({ path: this.redirect || '/', query: this.otherQuery })
              // this.$router.push({ path: forward || '/' })
            })
            .catch(() => {
              this.loading = false
              this.loginForm.vcode = false
            })
        } else {
          return false
        }
      })
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur]
        }
        return acc
      }, {})
    }
  }
}
</script>

<style type="text/scss" lang="scss" scoped>
.title{
  text-align: center;
  font-size: 25px;
  height: 60px;
  line-height: 60px;
  font-weight: bold;
  font-family: "Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei","微软雅黑",Arial,sans-serif;
}
@import '../../styles/mixin';
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.bg {
  position: absolute;
  z-index: -1;
  width: 100%;
  height: 100%;
}

$form_width: 325px;
.login-form {
  position: absolute;
  z-index: 1;
  left: 50%;
  top: 100px;
  padding: 20px 15px;
  margin-left: -(($form_width + 30px)/2);
  width: $form_width;
  background-color: #fff;
  @include box-shadow(0 0 15px 2px #d8dce5)
}
.login-logo {
  width: 100%;
  height: 110px;
}
.login-logo-img {
  width: 300px;
  height: 100px;
  margin: 0 auto;
  display: block;
}

.login-input {
  margin-top: 20px;
  .el-button {
    width: 100%;
  }
  ///deep/ .el-input-group__append {
  //  padding: 0;
  //  margin: 0;
  //  border: 0;
  //}
  .login-validcode-img {
    width: 90px;
    height: 32px;
    display: block;
    cursor: pointer;
  }
}
.set-language {
  cursor: pointer;
}
</style>
