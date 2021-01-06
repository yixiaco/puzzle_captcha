<!--拼图验证码组件-->
<template>
  <div>
    <el-button v-if="complete" type="success" disabled plain style="width:100%">已完成图片验证</el-button>
    <el-button v-if="!complete" style="width:100%" @click="isShow=true">点击完成图片验证</el-button>
    <PuzzleCode
      :bind="$attrs"
      :show="isShow"
      success-text="验证成功"
      fail-text="验证失败"
      slider-text="拖动滑块完成拼图"
      @success="success"
      @close="close"
    />
  </div>
</template>

<script>
import PuzzleCode from '@/components/PuzzleCode'
export default {
  components: {
    PuzzleCode
  },
  props: {
    complete: {
      type: Boolean,
      required: true,
      default: false
    }
  },
  data() {
    return {
      isShow: false
    }
  },
  methods: {
    // 用户通过了验证
    success(x) {
      this.isShow = false // 通过验证后，需要手动隐藏模态框
      this.$emit('success', x)
    },
    // 用户点击遮罩层，应该关闭模态框
    close() {
      this.isShow = false
      this.$emit('close')
    },
    fail(x) {
      this.$emit('fail', x)
    }
  }
}
</script>

<style scoped>

</style>
