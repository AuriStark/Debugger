<template>
  <div class="main-card q-ml-md q-mr-sm q-pt-sm q-pb-sm h-100">
    <q-card class="h-100 d-flex flex-dir-col">
      <q-fab icon="crop_free" direction="up" color="accent" class="fixed-fab">
        <q-fab-action color="primary" icon="view_stream" @click="zoom(0)" />
        <q-fab-action color="primary" icon="zoom_in" @click="zoom(1)" />
        <q-fab-action color="primary" icon="zoom_out" @click="zoom(-1)" />
      </q-fab>

      <q-card v-if="processing" dark class="working-box q-pa-sm">
        <q-spinner-hourglass size="2em" color="white" />
        Processing
      </q-card>

      <div class="row q-gutter-x-sm justify-end q-pt-sm q-pb-sm q-pl-md q-pr-md">
        <q-btn outline :disable="disableButton" color="positive" label="Continue" @click="sendContinue"/>
        <q-btn outline :disable="disableButton" color="info" label="Next step" @click="nextStep" />
        <q-btn outline :disable="disableButton" color="accent" label="Wake-up all" @click="wakeUpAll" />
      </div>

      <q-separator />

      <q-scroll-area
        class="main-content"
        :thumb-style="thumbStyle"
        :bar-style="barStyle"
      >
        <div id="flows" class="row q-pa-md flows-content" ref="graphs">
          <Node
            :flowOrder="flowOrder"
            :flowState="flowState"
            :flowId="firstFlowId"
            :currentExecuted="currentExecuted"
            :selectFlow="selectFlow"
            v-if="firstFlowId"
            :first="true"
            :selectedFlow="selectedFlow"
          ></Node>
        </div>
      </q-scroll-area>
    </q-card>
  </div>
</template>

<script>
import Panzoom from "@panzoom/panzoom";

import Node from "../components/Node.vue";

export default {
  components: {
    Node,
  },
  data: function () {
    return {
      thumbStyle: {
        right: "4px",
        borderRadius: "5px",
        backgroundColor: "#027be3",
        width: "7px",
        opacity: 0.75,
      },

      barStyle: {
        right: "2px",
        borderRadius: "9px",
        backgroundColor: "#027be3",
        width: "12px",
        opacity: 0.2,
      },
      flowState: {},
      flowOrder: [],
    };
  },
  props: {
    currentExecuted: null,
    selectedFlow: null,
    processing: false,
    stopped: false
  },

  computed: {
    firstFlowId: function () {
      return this.flowOrder[0];
    },

    disableButton: function(){
      return (this.selectedFlow == null || this.stopped);
    }
  },

  methods: {
    zoom(level) {
      switch (level) {
        case 0:
          this.panzoom.reset();
          break;
        case 1:
          this.panzoom.zoomIn();
          break;
        default:
          this.panzoom.zoomOut();
          break;
      }
    },

    render(flowState, flowOrder) {
      if (flowState) {
        this.flowState = flowState;
      }
      if (flowOrder) {
        this.flowOrder = flowOrder;
      }
    },

    selectFlow(id) {
      this.$emit("selectFlow", id);
    }, 

    sendContinue() {
      //emit a signal to send a continue message to the backend
      this.$emit("sendContinue");
    },
    nextStep() {
      //emit a signal to tell the backend to execute the next node
      this.$emit("nextStep");
    },
    wakeUpAll() {
      //emit a signal to tell the backend to wakeup all the children of a process
      this.$emit("wakeUpAll");
    },
  },

  mounted: function () {
    this.panzoom = Panzoom(document.getElementById("flows"), {
      maxScale: 5,
    });
  },

  watch: {
    flowOrder: function () {
      this.render();
    },
  },
};
</script>

<style lang="scss" scoped>
.main-card {
  display: flex;
  flex-direction: column;

  .working-box{
    position: absolute;
    bottom: 0px;
    left: 50%;
    transform: translate(-50%, -50%);
    border-radius: 10px;
    background-color: #614bb8;
    border: 1px solid #311d80;
  }

  .main-content {
    flex: 1;

    .flows-content {
      width: max-content;
    }
  }
}
</style>