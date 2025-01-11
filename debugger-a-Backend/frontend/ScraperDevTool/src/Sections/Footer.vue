<template>
  <q-footer class="bg-white" bordered>
    <q-bar class="bg-white">
        <q-btn
          dense
          outline
          color="primary"
          size="xs"
          :icon="(ldVisible? 'menu_open' : 'chevron_right')"
          @click="toggleLeftDrawer"
        >
          <q-tooltip v-if="ldVisible" transition-show="flip-right" transition-hide="flip-left">
            Close the left drawer
          </q-tooltip>
          <q-tooltip transition-show="flip-right" transition-hide="flip-left">
            <span v-if="ldVisible">Close the left drawer</span>
            <span v-else>Show the left drawer</span>
          </q-tooltip>
        </q-btn>
      <q-space />
      <q-btn
        dense
        color="primary"
        v-if="cvBtbVis"
        size="xs"
        label="Show console"
        @click="toggleShow"
      />
        <q-btn
          dense
          outline
          color="primary"
          size="xs"
          :icon="(rdVisible? 'chevron_right' : 'chevron_left')"
          @click="toggleRightDrawer"
        >
          <q-tooltip transition-show="flip-right" transition-hide="flip-left">
            <span v-if="rdVisible">Close the right drawer</span>
            <span v-else>Show the right drawer</span>
          </q-tooltip>
        </q-btn>
    </q-bar>
  </q-footer>
</template>

<script>
export default {
  data: function () {
    return {
    };
  },
  props: {
    cVisible: false,
    ldVisible: false,
    rdVisible: false,
  },
  methods: {
    toggleShow() {
      this.$emit("toggleShow");
    },
    toggleLeftDrawer(){
        this.$emit("toggleLeftDrawer");
    },
    toggleRightDrawer(){
        this.$emit("toggleRightDrawer");
    },
  },
  computed: {
    cvBtbVis: function () {
      return !this.cVisible;
    },
  },
};
</script>