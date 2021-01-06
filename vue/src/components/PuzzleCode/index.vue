<template>
  <!-- 本体部分 -->
  <div
    :id="id"
    :class="['vue-puzzle-vcode', { show_: show }]"
    @mousedown="onCloseMouseDown"
    @mouseup="onCloseMouseUp"
    @touchstart="onCloseMouseDown"
    @touchend="onCloseMouseUp"
  >
    <div class="vue-auth-box_" :style="{'border-radius':borderRadius+'px'}" @mousedown.stop @touchstart.stop>
      <div class="auth-body_" :style="`height: ${canvasHeight}px`">
        <!-- 主图，有缺口 -->
        <img ref="img1" :src="image1" alt="" :width="canvasWidth" :height="canvasHeight">
        <!-- 小图 -->
        <img
          ref="img2"
          :src="image2"
          class="auth-canvas2_"
          :height="canvasHeight"
          :style="`transform:translateX(${styleWidth - sliderBaseSize}px)`"
          alt=""
        >
        <div :class="['loading-box_', { hide_: !loading }]">
          <div class="loading-gif_">
            <span />
            <span />
            <span />
            <span />
            <span />
          </div>
        </div>
        <div :class="['info-box_', { show: infoBoxShow }, { fail: infoBoxFail }]">
          {{ infoText }}
        </div>
        <div
          :class="['flash_', { show: isSuccess }]"
          :style="
            `transform: translateX(${
              isSuccess
                ? `${canvasWidth + canvasHeight * 0.578}px`
                : `-${canvasHeight * 0.578}px`
            }) skew(-30deg, 0);`
          "
        />
        <img class="reset_" :src="resetSvg" @click="reset">
      </div>
      <div class="auth-control_">
        <div class="range-box" :style="`height:${sliderBaseSize}px`">
          <div class="range-text">{{ sliderText }}</div>
          <div ref="range-slider" class="range-slider" :style="`width:${styleWidth}px`">
            <div :class="['range-btn', { isDown: mouseDown }]" :style="`width:${sliderBaseSize}px`" @mousedown="onRangeMouseDown($event)" @touchstart="onRangeMouseDown($event)">
              <div />
              <div />
              <div />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { captcha, captchaVerify } from '@/api/common/common.js'

const resetSvg = require('@/components/PuzzleCode/reset.png')
export default {
  name: 'PuzzleCode',
  /** 父级参数 **/
  props: {
    id: { type: String, default: undefined },
    canvasWidth: { type: Number, default: 280 }, // 主canvas的宽
    canvasHeight: { type: Number, default: 150 }, // 主canvas的高
    // 是否出现，由父级控制
    show: { type: Boolean, default: false },
    sliderSize: { type: Number, default: 35 }, // 滑块的大小
    successText: {
      type: String,
      default: '验证通过！'
    },
    failText: {
      type: String,
      default: '验证失败，请重试'
    },
    sliderText: {
      type: String,
      default: '拖动滑块完成拼图'
    },
    borderRadius: {
      type: Number,
      default: 10
    },
    captcha: {
      type: Function,
      default: captcha
    },
    captchaVerify: {
      type: Function,
      default: captchaVerify
    }
  },
  /** 私有数据 **/
  data() {
    return {
      image1: undefined, // 大图
      image2: undefined, // 小图
      mouseDown: false, // 鼠标是否在按钮上按下
      startWidth: 50, // 鼠标点下去时父级的width
      startX: 0, // 鼠标按下时的X
      newX: 0, // 鼠标当前的偏移X
      loading: true, // 是否正在加载中，主要是等图片onload
      isCanSlide: false, // 是否可以拉动滑动条
      error: false, // 图片加在失败会出现这个，提示用户手动刷新
      infoBoxShow: false, // 提示信息是否出现
      infoText: '', // 提示等信息
      infoBoxFail: false, // 是否验证失败
      timer1: null, // setTimout1
      closeDown: false, // 为了解决Mac上的click BUG
      isSuccess: false, // 验证成功
      resetSvg,
      isSubmit: false // 是否正在验证中
    }
  },
  /** 计算属性 **/
  computed: {
    // styleWidth是底部用户操作的滑块的父级，就是轨道在鼠标的作用下应该具有的宽度
    styleWidth() {
      const w = this.startWidth + this.newX - this.startX
      return w < this.sliderBaseSize
        ? this.sliderBaseSize
        : w > this.canvasWidth
          ? this.canvasWidth
          : w
    },
    // 处理一下sliderSize，弄成整数，以免计算有偏差
    sliderBaseSize() {
      return Math.max(
        Math.min(
          Math.round(this.sliderSize),
          Math.round(this.canvasWidth * 0.5)
        ),
        10
      )
    }
  },

  /** 监听 **/
  watch: {
    show(newV) {
      // 每次出现都应该重新初始化
      if (newV) {
        document.body.classList.add('vue-puzzle-overflow')
        this.reset()
      } else {
        document.body.classList.remove('vue-puzzle-overflow')
      }
    }
  },

  /** 生命周期 **/
  mounted() {
    document.body.appendChild(this.$el)
    document.addEventListener('mousemove', this.onRangeMouseMove, false)
    document.addEventListener('mouseup', this.onRangeMouseUp, false)

    document.addEventListener('touchmove', this.onRangeMouseMove, {
      passive: false
    })
    document.addEventListener('touchend', this.onRangeMouseUp, false)
    if (this.show) {
      document.body.classList.add('vue-puzzle-overflow')
      this.reset()
    }
  },
  beforeDestroy() {
    clearTimeout(this.timer1)
    document.body.removeChild(this.$el)
    document.removeEventListener('mousemove', this.onRangeMouseMove, false)
    document.removeEventListener('mouseup', this.onRangeMouseUp, false)

    document.removeEventListener('touchmove', this.onRangeMouseMove, {
      passive: false
    })
    document.removeEventListener('touchend', this.onRangeMouseUp, false)
  },

  /** 方法 **/
  methods: {
    // 关闭
    onClose() {
      if (!this.mouseDown) {
        clearTimeout(this.timer1)
        this.$emit('update:show', false)
        this.$emit('close')
      }
    },
    onCloseMouseDown() {
      this.closeDown = true
    },
    onCloseMouseUp() {
      if (this.closeDown) {
        this.onClose()
      }
      this.closeDown = false
    },
    // 鼠标按下准备拖动
    onRangeMouseDown(e) {
      if (this.isCanSlide) {
        this.mouseDown = true
        this.startWidth = this.$refs['range-slider'].clientWidth
        this.newX = e.clientX || e.changedTouches[0].clientX
        this.startX = e.clientX || e.changedTouches[0].clientX
      }
    },
    // 鼠标移动
    onRangeMouseMove(e) {
      if (this.mouseDown) {
        e.preventDefault()
        this.newX = e.clientX || e.changedTouches[0].clientX
      }
    },
    // 鼠标抬起
    onRangeMouseUp() {
      if (this.mouseDown) {
        this.mouseDown = false
        this.submit()
      }
    },
    /**
     * 开始进行
     */
    init() {
      this.loading = true
      this.isCanSlide = false
      this.captcha().then(res => {
        this.image1 = res.image1
        this.image2 = res.image2
        this.loading = false
        this.isCanSlide = true
        this.startTime = new Date().getTime()
      })
    },
    // 开始判定
    submit() {
      // 关闭拖动
      this.isCanSlide = false
      this.isSubmit = true
      const x = this.newX - this.startX
      this.loading = true
      this.captchaVerify({ x, width: this.canvasWidth }).then(res => {
        this.isSubmit = false
        this.loading = false
        if (res.success) {
          // 成功
          this.infoText = this.successText
          this.infoBoxFail = false
          this.infoBoxShow = true
          this.isCanSlide = false
          this.isSuccess = true
          // 成功后准备关闭
          clearTimeout(this.timer1)
          this.timer1 = setTimeout(() => {
            this.$emit('update:show', false)
            // 成功的回调
            this.$emit('success', x)
          }, 800)
        } else {
          // 失败
          this.infoText = this.failText
          this.infoBoxFail = true
          this.infoBoxShow = true
          this.isCanSlide = false
          // 失败的回调
          this.$emit('fail', x)
          // 800ms后重置
          clearTimeout(this.timer1)
          this.timer1 = setTimeout(() => {
            this.reset()
          }, 800)
        }
      })
    },
    // 重置
    reset() {
      this.infoBoxFail = false
      this.infoBoxShow = false
      this.isCanSlide = true
      this.isSuccess = false
      this.startWidth = this.sliderBaseSize // 鼠标点下去时父级的width
      this.startX = 0 // 鼠标按下时的X
      this.newX = 0 // 鼠标当前的偏移X
      this.init()
    }
  }
}
</script>
<style lang="scss">
.vue-puzzle-vcode {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 999;
  opacity: 0;
  pointer-events: none;
  transition: opacity 200ms;
  &.show_ {
    opacity: 1;
    pointer-events: auto;
  }
}
.vue-auth-box_ {
  position: absolute;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 20px;
  background: #fff;
  user-select: none;
  border-radius: 3px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  .auth-body_ {
    position: relative;
    overflow: hidden;
    border-radius: 3px;
    .loading-box_ {
      position: absolute;
      top: 0;
      left: 0;
      bottom: 0;
      right: 0;
      background-color: rgba(0, 0, 0, 0.8);
      z-index: 20;
      opacity: 1;
      transition: opacity 200ms;
      display: flex;
      align-items: center;
      justify-content: center;
      &.hide_ {
        opacity: 0;
        pointer-events: none;
        .loading-gif_ {
          span {
            animation-play-state: paused;
          }
        }
      }
      .loading-gif_ {
        flex: none;
        height: 5px;
        line-height: 0;
        @keyframes load {
          0% {
            opacity: 1;
            transform: scale(1.3);
          }
          100% {
            opacity: 0.2;
            transform: scale(0.3);
          }
        }
        span {
          display: inline-block;
          width: 5px;
          height: 100%;
          margin-left: 2px;
          border-radius: 50%;
          background-color: #888;
          animation: load 1.04s ease infinite;
          &:nth-child(1) {
            margin-left: 0;
          }
          &:nth-child(2) {
            animation-delay: 0.13s;
          }
          &:nth-child(3) {
            animation-delay: 0.26s;
          }
          &:nth-child(4) {
            animation-delay: 0.39s;
          }
          &:nth-child(5) {
            animation-delay: 0.52s;
          }
        }
      }
    }
    .info-box_ {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 24px;
      line-height: 24px;
      text-align: center;
      overflow: hidden;
      font-size: 13px;
      background-color: #83ce3f;
      opacity: 0;
      transform: translateY(24px);
      transition: all 200ms;
      color: #fff;
      z-index: 10;
      &.show {
        opacity: 0.95;
        transform: translateY(0);
      }
      &.fail {
        background-color: #ce594b;
      }
    }
    .auth-canvas2_ {
      position: absolute;
      top: 0;
      left: 0;
      z-index: 2;
    }
    .auth-canvas3_ {
      position: absolute;
      top: 0;
      left: 0;
      opacity: 0;
      transition: opacity 600ms;
      z-index: 3;
      &.show {
        opacity: 1;
      }
    }
    .flash_ {
      position: absolute;
      top: 0;
      left: 0;
      width: 30px;
      height: 100%;
      background-color: rgba(255, 255, 255, 0.1);
      z-index: 3;
      &.show {
        transition: transform 600ms;
      }
    }
    .reset_ {
      position: absolute;
      top: 2px;
      right: 2px;
      width: 35px;
      height: auto;
      z-index: 12;
      cursor: pointer;
      transition: transform 200ms;
      transform: rotate(0deg);
      &:hover {
        transform: rotate(-90deg);
      }
    }
  }
  .auth-control_ {
    .range-box {
      position: relative;
      width: 100%;
      background-color: #eef1f8;
      margin-top: 20px;
      border-radius: 3px;
      box-shadow: 0 0 8px rgba(240, 240, 240, 0.6) inset;
      .range-text {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 14px;
        color: #b7bcd1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        text-align: center;
        width: 100%;
      }
      .range-slider {
        position: absolute;
        height: 100%;
        width: 50px;
        background-color: rgba(106, 160, 255, 0.8);
        border-radius: 3px;
        .range-btn {
          position: absolute;
          display: flex;
          align-items: center;
          justify-content: center;
          right: 0;
          width: 50px;
          height: 100%;
          background-color: #fff;
          border-radius: 3px;
          box-shadow: 0 0 4px #ccc;
          cursor: pointer;
          & > div {
            width: 0;
            height: 40%;

            transition: all 200ms;
            &:nth-child(2) {
              margin: 0 4px;
            }
            border: solid 1px #6aa0ff;
          }
          &:hover,
          &.isDown {
            & > div:first-child {
              border: solid 4px transparent;
              height: 0;
              border-right-color: #6aa0ff;
            }
            & > div:nth-child(2) {
              border-width: 3px;
              height: 0;
              border-radius: 3px;
              margin: 0 6px;
              border-right-color: #6aa0ff;
            }
            & > div:nth-child(3) {
              border: solid 4px transparent;
              height: 0;
              border-left-color: #6aa0ff;
            }
          }
        }
      }
    }
  }
}
.vue-puzzle-overflow {
  overflow: hidden !important;
}
</style>
