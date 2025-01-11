<template>
  <div class="bottom-split">
    <q-bar class="consoleBar bg-white">
      <div class="row q-gutter-x-sm">
        <q-btn
          flat
          color="positive"
          size="sm"
          icon="flag"
          @click="sendReady"
          :disable="!connected || stopped"
        >
          <q-tooltip transition-show="flip-right" transition-hide="flip-left">
            Ready
          </q-tooltip>
        </q-btn>

        <q-btn
          flat
          color="negative"
          size="sm"
          icon="stop"
          @click="stopProcess"
          :disable="!ready"
        >
          <q-tooltip transition-show="flip-right" transition-hide="flip-left">
            Stop
          </q-tooltip>
        </q-btn>
      </div>

      <q-space />

      <q-btn
        dense
        outline
        color="primary"
        size="sm"
        icon="delete"
        @click="clearConsole"
      >
        <q-tooltip transition-show="flip-right" transition-hide="flip-left">
          Clear output
        </q-tooltip>
      </q-btn>
      <q-btn
        dense
        outline
        color="primary"
        size="sm"
        icon="arrow_drop_down"
        @click="toggleShow"
      >
        <q-tooltip transition-show="flip-right" transition-hide="flip-left">
          Hide console
        </q-tooltip>
      </q-btn>
    </q-bar>

    <q-separator />

    <div class="consoleZoneContainer">
      <div class="consoleZone q-pa-sm" ref="consoleZone">
        <LogItem
          v-for="(item, index) in logs"
          :key="index"
          :log="item"
        ></LogItem>
      </div>
    </div>
  </div>
</template>

<script>
import LogItem from "../components/LogItem.vue";

export default {
  components: {
    LogItem,
  },
  methods: {
    toggleShow() {
      //emit a signal to toggle the drawer
      this.$emit("toggleShow");
    },
    sendReady() {
      //emit a signal to send a continue message to the backend
      this.$emit("sendReady");
    },
    clearConsole() {
      //emit a signal to clear the global log variable
      this.$emit("clearConsole");
    },
    stopProcess() {
      //emit a signal to tell the backend to stop the process
      this.$emit("stopProcess");
    },
  },

  props: {
    flowState: {
      default: {},
    },
    logs: {
      default: [],
    },
    connected: {
      default: false,
    },
    ready: {
      default: false,
    },
    stopped: {
      default: false,
    },
  },

  computed:{
    numberLogs: function(){
      return this.logs.length
    }
  },

  watch: {
    numberLogs: function(){
      //Automatically scroll to bottom
      var objDiv = this.$refs["consoleZone"];
      objDiv.scrollTop = objDiv.scrollHeight;
    }
  }
};
</script>

<style lang="scss" scoped>
.bottom-split {
  background-color: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;

  .consoleBar {
    height: 28px;
  }
  .consoleZoneContainer {
    flex: 1;
    position: relative;

    .consoleZone {
      height: 100%;
      position: absolute;
      top: auto;
      border: 0;
      left: 0;
      right: 0;
      overflow: auto;
    }
  }
}
</style>