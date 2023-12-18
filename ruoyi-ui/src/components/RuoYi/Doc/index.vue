<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'RuoYiDoc',
  data() {
    return {
      url: 'https://waterrespect.github.io/'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
