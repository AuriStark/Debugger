<template>
  <q-layout view="hHh lpr fFf">
    <Header :connected="connected"></Header>

    <LeftDrawer
      :opened="leftDrawerOpen"
      :currentNode="currentNode"
      :breakpoints="breakpoints"
      @updateBp="updateBp"
    ></LeftDrawer>
    <RightDrawer
      :opened="rightDrawerOpen"
      :currentFlow="currentFlow"
      :flowState="flowState"
    ></RightDrawer>

    <q-page-container class="page-container" style="height: 100vh">
      <q-splitter
        v-model="splitterVModel"
        horizontal
        style="height: 100%"
        :limits="vSpLimit"
      >
        <template v-slot:before>
          <div class="top-split q-pa-sm">
            <div class="row q-gutter-x-md q-mr-md q-ml-sm q-pt-sm q-pb-sm">
              <q-input
                v-model.number="port"
                outlined
                dense
                type="number"
                label="Port"
                :disable="connected"
              />
              <q-btn
                :color="connected ? 'negative' : 'positive'"
                :label="connected ? 'Disconnect' : 'Connect'"
                @click="toggleConnection"
              >
                <q-tooltip
                  transition-show="flip-right"
                  transition-hide="flip-left"
                >
                  <span v-if="connected"
                    >Disconnect to the scraperflow's process</span
                  >
                  <span v-else>Connect to the scraperflow's process</span>
                </q-tooltip>
              </q-btn>
              <q-btn
                outline
                color="primary"
                label="Ready"
                :disable="ready"
                @click="sendReady"
                v-if="false"
              >
                <q-tooltip
                  transition-show="flip-right"
                  transition-hide="flip-left"
                >
                  Activate the ready state
                </q-tooltip>
              </q-btn>
            </div>

            <div class="hSplitContainer">
              <q-splitter class="hSplit" v-model="splitterHModel">
                <template v-slot:before>
                  <LeftZone
                    ref="leftZone"
                    @chooseNode="chooseNode"
                    :breakpoints="breakpoints"
                    @sendBp="sendBreakpoints"
                    :connected="connected"
                    :stopped="stopped"
                  ></LeftZone>
                </template>

                <template v-slot:separator>
                  <q-avatar
                    color="primary"
                    text-color="white"
                    size="25px"
                    icon="drag_indicator"
                  />
                </template>

                <template v-slot:after>
                  <RightZone
                    ref="rightZone"
                    :currentExecuted="currentExecuted"
                    @selectFlow="selectFlow"
                    @sendContinue="sendContinue"
                    @nextStep="nextStep"
                    :selectedFlow="currentFlow"
                    :processing="processing"
                    :stopped="stopped"
                    @wakeUpAll="wakeUpAll"
                  ></RightZone>
                </template>
              </q-splitter>
            </div>
          </div>
        </template>

        <template v-slot:after>
          <BottomZone
            v-show="consoleVisible"
            @toggleShow="toggleConsole"
            @sendReady="sendReady"
            @clearConsole="clearConsole"
            @stopProcess="stopProcess"
            :flowState="flowState"
            :logs="logs"
            :connected="connected"
            :ready="ready"
            :stopped="stopped"
          ></BottomZone>
        </template>
      </q-splitter>
    </q-page-container>

    <Footer
      @toggleLeftDrawer="toggleLeftDrawer"
      @toggleRightDrawer="toggleRightDrawer"
      @toggleShow="toggleConsole"
      :cVisible="consoleVisible"
      :ldVisible="leftDrawerOpen"
      :rdVisible="rightDrawerOpen"
    ></Footer>
  </q-layout>
</template>

<script>
import Footer from "../Sections/Footer.vue";
import LeftDrawer from "../Sections/LeftDrawer.vue";
import RightDrawer from "../Sections/RightDrawer.vue";
import Header from "../Sections/Header.vue";
import LeftZone from "../Sections/LeftZone.vue";
import RightZone from "../Sections/RightZone.vue";
import BottomZone from "../Sections/BottomZone.vue";

import { Notify } from "quasar";

export default {
  name: "Index",
  components: {
    Footer,
    LeftDrawer,
    RightDrawer,
    Header,
    LeftZone,
    RightZone,
    BottomZone,
  },
  data: function () {
    return {
      leftDrawerOpen: false,
      rightDrawerOpen: false,
      splitterVModel: 70,
      splitterHModel: 50,

      connected: false,
      ready: false,
      stopped: false,
      port: 8890,
      consoleVisible: true,
      socket: null,
      breakpoints: {},
      currentNode: null,
      currentFlow: null,
      flowState: {},
      flowOrder: [],
      currentExecuted: null,
      logs: [],

      refreshTimeout: null,
      processing: false,
    };
  },

  computed: {
    vSpLimit: function () {
      if (this.consoleVisible) {
        return [10, 90];
      } else {
        return [0, Infinity];
      }
    },
  },

  methods: {
    toggleLeftDrawer() {
      this.leftDrawerOpen = !this.leftDrawerOpen;
    },
    toggleRightDrawer() {
      this.rightDrawerOpen = !this.rightDrawerOpen;
    },
    toggleConsole() {
      this.consoleVisible = !this.consoleVisible;
    },
    chooseNode(title) {
      this.currentNode = title;
      this.leftDrawerOpen = true;
    },
    selectFlow(id) {
      this.currentFlow = id;
      this.rightDrawerOpen = true;
    },
    updateBp(state) {
      let bps = { ...this.breakpoints };

      if (bps[this.currentNode] == undefined) {
        bps[this.currentNode] = {};
      }

      bps[this.currentNode].before = state.before;
      bps[this.currentNode].after = state.after;

      this.breakpoints = bps;
    },
    resetTimeout() {
      if (this.refreshTimeout) {
        clearTimeout(this.refreshTimeout);
        this.refreshTimeout = null;
      }
    },
    clearConsole() {
      this.logs = [];
    },

    // API
    toggleConnection() {
      if (this.socket) {
        this.socket.close();
        this.socket = null;
      } else {
        // Create WebSocket connection.
        this.socket = new WebSocket(`ws://localhost:${this.port}`);

        // Connection opened
        this.socket.addEventListener("open", (event) => {
          this.connected = true;

          // Show notification
          Notify.create({
            message: "Connection to the process established",
            progress: true,
            color: "positive",
            textColor: "white",
          });

          // Ask for the instances
          this.getInstances();

          // Ask for the graph
          this.getGraphs();

          // Ask for the state
          this.getState();
        });

        // Connection closed
        this.socket.addEventListener("close", (event) => {
          this.connected = false;
          this.ready = false;
          this.socket = null;

          // Show notification
          Notify.create({
            message: "Connection to the process canceled",
            progress: true,
            color: "negative",
            textColor: "white",
          });
        });

        // Listen for messages
        this.socket.addEventListener("message", (e) => {
          this.handleMessage(e);
        });
      }
    },

    handleMessage(event) {
      let json = JSON.parse(event.data);
      //console.log("Message from server ", json);

      if (json.type == "currentFlow") {
        let data = json.data;
        this.currentExecuted = data.flowId;
      }

      if (json.type == "nodePre") {
        this.processing = true;

        let data = json.data;
        let fId = data.flowMap.flowId;
        let pfId = data.flowMap.parentId;
        this.currentExecuted = fId;

        let stateItem = {
          children: [],
          flowId: fId,
          nodeAddress: data.node.address,
          nodePre: data,
        };
        this.flowState[fId] = stateItem;
        this.flowOrder.push(fId);

        if (this.flowState[pfId]) {
          this.flowState[pfId].children.push(fId);
        }

        this.resetTimeout();

        let bindedFunction = function () {
          let flowState = JSON.parse(JSON.stringify(this.flowState));
          let flowOrder = JSON.parse(JSON.stringify(this.flowOrder));

          this.$refs.rightZone.render(flowState, flowOrder);
          this.processing = false;
          this.resetTimeout();
        }.bind(this);

        this.refreshTimeout = setTimeout(bindedFunction, 1000);
      }

      if (json.type == "nodePost") {
        let data = json.data;
        let fId = data.flowMap.flowId;
        this.currentExecuted = fId;
        this.flowState[fId].nodePost = data;

        this.resetTimeout();

        let bindedFunction = function () {
          let flowState = JSON.parse(JSON.stringify(this.flowState));
          let flowOrder = JSON.parse(JSON.stringify(this.flowOrder));

          this.$refs.rightZone.render(flowState, flowOrder);
          this.processing = false;
          this.resetTimeout();
        }.bind(this);

        this.refreshTimeout = setTimeout(bindedFunction, 1000);
      }

      if (json.type == "graphs") {
        this.$refs.leftZone.render(json.data);
      }

      if (json.type == "state") {
        this.flowState = json.data.stack;
        this.flowOrder = json.data.order;
        this.ready = json.data.ready;
        this.$refs.rightZone.render(this.flowState, this.flowOrder);
      }

      if (json.type == "readyState") {
        this.ready = json.data.ready;
      }

      if (json.type == "stoppedState") {
        this.stopped = json.data.stopped;

        if(this.stopped){
          this.ready = false
        }
      }

      if (json.type == "beforeBreakpoint") {
        let data = json.data;
        let log = `Breaking (before:) on ${data.flowId}`;
        this.logs.push({
          type: "break",
          message: log,
          time: new Date().toISOString(),
        });
      }

      if (json.type == "afterBreakpoint") {
        let data = json.data;
        let log = `Breaking (after:) on ${data.flowId}`;
        this.logs.push({
          type: "break",
          message: log,
          time: new Date().toISOString(),
        });
      }

      if (json.type == "beforeBreakpointContinue") {
        let data = json.data;
        let log = `Continue (before:) on ${data.flowId}`;
        this.logs.push({
          type: "break",
          message: log,
          time: new Date().toISOString(),
        });
      }

      if (json.type == "afterBreakpointContinue") {
        let data = json.data;
        let log = `Continue (after:) on ${data.flowId}`;
        this.logs.push({
          type: "break",
          message: log,
          time: new Date().toISOString(),
        });
      }

      if (json.type == "log") {
        let data = json.data;
        let log = data.message;
        this.logs.push(log);
      }
    },

    sendContinue(id = null) {
      var params = {
        command: "setContinue",
        data: {
          flowId: id ?? this.currentFlow,
        },
      };
      params = JSON.stringify(params);
      this.socket.send(params);
    },

    nextStep() {
      var params = {
        command: "stepwiseExecution",
        data: {
          flowId: this.currentFlow,
        },
      };
      params = JSON.stringify(params);
      this.socket.send(params);
    },

    sendReady() {
      var params = {
        command: "setReady",
        data: {},
      };
      params = JSON.stringify(params);
      this.socket.send(params);
    },

    sendBreakpoints() {
      var params = {
        command: "setBreakpoints",
        data: {
          breakpoints: this.breakpoints,
        },
      };
      params = JSON.stringify(params);
      this.socket.send(params);
    },

    getGraphs() {
      var params = {
        command: "getGraphs",
        data: {},
      };
      this.socket.send(JSON.stringify(params));
    },

    getState() {
      var params = {
        command: "getState",
        data: {},
      };
      this.socket.send(JSON.stringify(params));
    },

    getInstances() {
      var params = {
        command: "requestSpecifications",
        data: {},
      };
      this.socket.send(JSON.stringify(params));
    },

    stopProcess() {
      var params = {
        command: "stopProcess",
        data: {},
      };
      this.socket.send(JSON.stringify(params));
    },

    wakeUpAll(){
      var params = {
        command: "wakeUpAll",
        data: {
          flowId: this.currentFlow,
        },
      };
      params = JSON.stringify(params);
      this.socket.send(params);
    }
  },

  watch: {
    consoleVisible: function (value) {
      if (value) {
        this.splitterVModel = 70;
      } else {
        this.splitterVModel = 100;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.page-container {
  background-color: $secondary;

  .top-split {
    height: 100%;
    display: flex;
    flex-direction: column;

    .hSplitContainer {
      flex: 1;
      position: relative;

      .hSplit {
        height: 100%;
        position: absolute;
        top: auto;
        border: 0;
        left: 0;
        right: 0;
      }
    }
  }
}
</style>