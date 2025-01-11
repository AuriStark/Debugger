<template>
  <q-drawer
    v-model="opened"
    side="right"
    bordered
    class="q-gutter-y-md q-pa-sm"
    :width="325"
  >
    <q-expansion-item
      class="shadow-1 overflow-hidden"
      style="border-radius: 30px"
      icon="explore"
      label="Flow"
      header-class="bg-primary text-white"
      expand-icon-class="text-white"
      default-opened
    >
      <q-list>
        <q-item>
          <q-item-section>
            <q-item-label>Flow's Id</q-item-label>
            <q-item-label caption lines="2">{{ currentFlow }}</q-item-label>
          </q-item-section>
        </q-item>
        <q-item>
          <q-item-section>
            <q-item-label>Node's Address</q-item-label>
            <q-item-label caption lines="2">{{ nodeName }}</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-expansion-item>

    <q-expansion-item
      class="shadow-1 overflow-hidden"
      style="border-radius: 30px 30px 0 0"
      icon="timer"
      label="Preprocessing"
      header-class="bg-primary text-white"
      expand-icon-class="text-white"
      default-opened
      v-if="hasPreInfo"
    >
      <q-list bordered class="rounded-borders">
        <q-expansion-item switch-toggle-side expand-separator label="Node">
          <q-separator />
          <q-card>
            <q-card-section class="q-pa-sm">
              <q-table
                class="w-100"
                title="Node's data"
                :rows="preNodeData"
                :columns="dataColumn"
                row-key="key"
              />
            </q-card-section>
          </q-card>
        </q-expansion-item>

        <q-expansion-item switch-toggle-side expand-separator label="Flow">
          <q-separator />
          <q-card>
            <q-card-section class="column q-gutter-y-sm q-pa-sm">
              <q-table
                class="w-100"
                :title="item['name']"
                :rows="item['data']"
                :columns="dataColumn"
                row-key="key"
                v-for="(item, index) in preFlowData"
                :key="index"
              />
            </q-card-section>
          </q-card>
        </q-expansion-item>
      </q-list>
    </q-expansion-item>

    <q-expansion-item
      class="shadow-1 overflow-hidden"
      style="border-radius: 30px 30px 0 0"
      icon="timer"
      label="Postprocessing"
      header-class="bg-primary text-white"
      expand-icon-class="text-white"
      default-opened
      v-if="hasPostInfo"
    >
      <q-list bordered class="rounded-borders">
        <q-expansion-item switch-toggle-side expand-separator label="Node">
          <q-separator />
          <q-card>
            <q-card-section class="q-pa-sm">
              <q-table
                class="w-100"
                title="Node's data"
                :rows="postNodeData"
                :columns="dataColumn"
                row-key="key"
              />
            </q-card-section>
          </q-card>
        </q-expansion-item>

        <q-expansion-item switch-toggle-side expand-separator label="Flow">
          <q-separator />
          <q-card>
            <q-card-section class="column q-gutter-y-sm q-pa-sm">
              <q-table
                class="w-100"
                :title="item['name']"
                :rows="item['data']"
                :columns="dataColumn"
                row-key="key"
                v-for="(item, index) in postFlowData"
                :key="index"
              />
            </q-card-section>
          </q-card>
        </q-expansion-item>
      </q-list>
    </q-expansion-item>
  </q-drawer>
</template>

<script>
export default {
  data: function () {
    return {
      dataColumn: [
        {
          name: "key",
          label: "Key",
          required: true,
          align: "left",
          field: (row) => row.key,
          format: (val) => `${val}`,
          sortable: true,
        },
        {
          name: "value",
          label: "Value",
          required: true,
          align: "right",
          field: (row) => row.value,
          format: (val) => `${val}`,
          sortable: true,
        },
      ],
    };
  },
  props: {
    opened: false,
    currentFlow: null,
    flowState: {
      default: {},
    },
  },

  computed: {
    flow: function () {
      return this.flowState[this.currentFlow];
    },

    nodeType: function () {
      return this.flow?.nodePre.node.nodeConfiguration.type;
    },

    nodeName: function () {
      return this.flow?.nodeAddress;
    },

    hasPostInfo: function () {
      return this.flow?.nodePost;
    },

    hasPreInfo: function () {
      return this.flow?.nodePre;
    },

    preNodeData: function () {
      if (this.flow?.nodePre) {
        let node = this.flow?.nodePre.node.nodeConfiguration;
        return this.getData(node);
      } else {
        return [];
      }
    },

    postNodeData: function () {
      if (this.flow?.nodePost) {
        let node = this.flow?.nodePost.node.nodeConfiguration;
        return this.getData(node);
      } else {
        return [];
      }
    },

    preFlowData: function () {
      if (this.flow?.nodePre) {
        let flow = this.flow?.nodePre.flowMap;

        let contextData = {
          name: "Context",
          data: this.getData(flow?.context),
        };
        let contentData = {
          name: "Content",
          data: this.getData(flow?.content),
        };
        return [contextData, contentData];
      } else {
        return [];
      }
    },

    postFlowData: function () {
      if (this.flow?.nodePost) {
        let flow = this.flow?.nodePost.flowMap;

        let contextData = {
          name: "Context",
          data: this.getData(flow?.context),
        };
        let contentData = {
          name: "Content",
          data: this.getData(flow?.content),
        };
        return [contextData, contentData];
      } else {
        return [];
      }
    },
  },

  methods: {
    getData(node) {
      let data = [];

      for (let key in node) {
        let item = {
          key: key,
          value: JSON.stringify(node[key], 2, null),
        };
        data.push(item);
      }
      return data;
    },
  },
};
</script>