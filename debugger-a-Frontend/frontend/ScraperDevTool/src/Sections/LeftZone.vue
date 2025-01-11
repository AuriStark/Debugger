<template>
  <div class="main-card q-ml-sm q-mr-md q-pt-sm q-pb-sm h-100">
    <q-card class="h-100 d-flex flex-dir-col">
      <q-fab
        v-model="fabOpened"
        persistent
        icon="crop_free"
        direction="up"
        color="accent"
        class="fixed-fab"
      >
        <q-fab-action color="primary" icon="view_stream" @click="zoom(0)" />
        <q-fab-action color="primary" icon="zoom_in" @click="zoom(1)" />
        <q-fab-action color="primary" icon="zoom_out" @click="zoom(-1)" />
      </q-fab>

      <div class="row justify-end q-pt-sm q-pb-sm q-pl-md q-pr-md">
        <q-btn
          outline
          color="primary"
          label="Send breakpoints"
          :disable="!connected || stopped"
          @click="sendBp"
        />
      </div>

      <q-separator />

      <q-scroll-area
        class="main-content"
        :thumb-style="thumbStyle"
        :bar-style="barStyle"
      >
        <div id="graphs" class="graphs-content" ref="graphs"></div>
      </q-scroll-area>
    </q-card>
  </div>
</template>

<script>
import Panzoom from "@panzoom/panzoom";

export default {
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

      graphData: "",
      fabOpened: false,
    };
  },

  props: {
    breakpoints: {},
    connected: false,
    stopped: false
  },

  methods: {
    render(data) {
      if (data) {
        this.graphData = data;
      }
      var viz = new Viz();

      viz
        .renderSVGElement(this.graphData.list[0], {})
        .then((element) => {
          document.getElementById("graphs").innerHTML = "";
          document.getElementById("graphs").appendChild(element);

          let nodeList = document.querySelectorAll("g.node");

          for (let node of nodeList) {
            let title = node.querySelector("title").innerHTML;

            node.addEventListener("click", (e) => {
              this.$emit("chooseNode", title);
            });
          }

          this.handleBreakpoints();
        })
        .catch((error) => {
          viz = new Viz();
          console.error(error);
        });
    },

    handleBreakpoints() {
      let nodeList = document.querySelectorAll("g.node");

      for (let node of nodeList) {
        let title = node.querySelector("title").innerHTML;
        let bp = this.breakpoints[title];

        if (bp) {
          if (bp.before) {
            node.classList.add("before-bp");
          } else {
            node.classList.remove("before-bp");
          }

          if (bp.after) {
            node.classList.add("after-bp");
          } else {
            node.classList.remove("after-bp");
          }
        }
      }
    },

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

    sendBp() {
      this.$emit("sendBp");
    },
  },

  watch: {
    breakpoints: function () {
      this.render();
    },
  },

  mounted: function () {
    this.panzoom = Panzoom(document.getElementById("graphs"), {
      maxScale: 5,
    });
  },
};
</script>

<style lang="scss" scoped>
.main-card {
  display: flex;
  flex-direction: column;

  .main-content {
    flex: 1;

    .graphs-content {
      display: flex;
      justify-content: center;
    }
  }
}
</style>