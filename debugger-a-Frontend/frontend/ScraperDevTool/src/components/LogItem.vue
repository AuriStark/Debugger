<template>
  <div class="log">
    <div class="log-type"></div>
    <div class="log-content">
      <div class="row q-gutter-x-sm log-text" :style="{ color: textColor }">
        <span>{{ log }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data: function () {
    return {};
  },
  props: {
    log: {
      default: " ",
    },
  },
  computed: {
    textColor: function () {
      switch (this.tag) {
        case "W":
          return "#F2C037";
        case "I":
          return "#000";
        case "S":
          return "#e64e60";
        default:
          return "#000";
      }
    },
    tag: function () {
      return this.log[0]?.toUpperCase();
    },
  },
};
</script>

<style lang="scss" scoped>
</style>