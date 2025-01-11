<template>
  <div>
    <q-card
      class="node-header q-mt-sm"
      :class="first ? '' : 'not-root'"
      :style="style"
      @click="select"
    >
      <q-card-section class="q-pa-sm">
        <q-btn
          dense
          outline
          rounded
          color="primary"
          size="xs"
          :icon="opened ? 'remove' : 'add'"
          @click="toggleChildren"
          v-if="haveChildren"
        >
        </q-btn>
        <q-badge class="q-ml-sm q-mr-sm" align="middle">{{ nodeType }}</q-badge>
        {{ nodeName }}
        <q-badge
          v-if="haveChildren"
          color="red"
          rounded
          floating
          :label="childNumber"
        />
      </q-card-section>
    </q-card>
    <div v-show="opened" class="children-list">
      <div class="child" v-for="(item, index) in nbVisibleChild" :key="index">
        <Node
          :flowOrder="flowOrder"
          :flowState="flowState"
          :flowId="getFlowId(index)"
          :selectFlow="selectFlow"
          :selectedFlow="selectedFlow"
        ></Node>
      </div>
      <q-btn
        class="q-mt-sm w-100"
        v-if="showMoreBtn"
        outline
        dense
        color="primary"
        label="Show More"
        @click="addNumberShowerChild"
      />
    </div>
  </div>
</template>

<script>
import Node from "./Node.vue";

export default {
  data: function () {
    return {
      opened: true,
      numberShowerChild: 50,
    };
  },
  computed: {
    style: function () {
      return {
        backgroundColor:
          this.nodeColor + (this.flowId == this.selectedFlow ? "" : "70"),
        color: "#fff",
        width: "fit-content",
      };
    },

    haveChildren: function () {
      return this.flow.children.length > 0;
    },

    childNumber: function () {
      return this.flow.children.length;
    },

    nodeType: function () {
      return this.flow.nodePre.node.nodeConfiguration.type;
    },

    nodeName: function () {
      return this.flow.nodeAddress;
    },

    sleepingBreakp: function () {
      return this.flow.nodePost == undefined;
    },

    nodeColor: function () {
      if (this.sleepingBreakp) {
        return "#F2C037";
      }
      if (this.currentExecuted == this.flow.flowId) {
        return "#21BA45";
      }
      return "#e64e60";
    },

    flow: function () {
      return this.flowState[this.flowId];
    },

    showMoreBtn: function () {
      return this.numberShowerChild < this.childNumber;
    },

    nbVisibleChild: function () {
      if (this.numberShowerChild > this.childNumber) {
        return this.childNumber;
      } else {
        return this.numberShowerChild;
      }
    },
  },
  props: {
    flowId: null,
    first: false,
    flowState: {
      default: {},
    },
    flowOrder: {
      default: [],
    },
    currentExecuted: null,
    selectFlow: {
      type: Function,
      default: () => {},
    },
    selectedFlow: {
      default: null,
    },
  },

  methods: {
    toggleChildren(e) {
      e.stopPropagation();
      this.opened = !this.opened;

      if (!this.opened) {
        this.numberShowerChild = 50;
      }
    },

    select() {
      this.selectFlow(this.flow.flowId);
    },

    addNumberShowerChild() {
      this.numberShowerChild = this.numberShowerChild + 50;
    },

    getFlowId(index){
      return this.flow?.children[index]
    }
  },

  watch: {
  },

  mounted: function () {
    if (this.childNumber > 4) {
      this.opened = false;
    }
  },
};
</script>

<style lang="scss" scoped>
.node-header {
  cursor: pointer;

  &.not-root {
    &::before {
      border-bottom: 1px dashed #000;
      border-left: 1px dashed #000;
      bottom: 50%;
      content: "";
      left: -13px;
      position: absolute;
      top: -8px;
      width: 13px;
      border-radius: 0 0 0 20px;
    }
    &::after {
      font-family: "Material Icons";
      content: "\e5cc";
      position: absolute;
      left: -8px;
      color: #000;
      top: 50%;
      transform: translateY(-50%);
    }
  }
}
.children-list {
  padding-left: 25px;

  .child {
    position: relative;
    &:not(:last-child) {
      &::after {
        border-left: 1px dashed;
        bottom: 0px;
        content: "";
        left: -13px;
        position: absolute;
        right: auto;
        top: 0;
        width: 2px;
      }
    }
  }
}
</style>